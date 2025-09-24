package com.syos.inventory.infrastructure.repository;

import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.repository.UserRepository;
import com.syos.inventory.domain.value.Password;
import com.syos.inventory.domain.value.UserRole;
import com.syos.inventory.domain.value.Username;
import com.syos.inventory.infrastructure.database.DatabaseManager;
import com.syos.shared.exceptions.technical.DatabaseException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * SQLite implementation of UserRepository
 */
public class SqliteUserRepository implements UserRepository {
    
    private static final Logger logger = Logger.getLogger(SqliteUserRepository.class.getName());
    private final DatabaseManager databaseManager;
    
    // SQL Queries
    private static final String INSERT_USER = 
        "INSERT INTO user (user_code, email, password_hash, password_salt, first_name, last_name, phone, address, role_id, is_active, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT role_id FROM role WHERE role_name = ?), ?, ?)";
    
    private static final String UPDATE_USER = 
        "UPDATE user SET email = ?, password_hash = ?, password_salt = ?, first_name = ?, last_name = ?, phone = ?, address = ?, " +
        "role_id = (SELECT role_id FROM role WHERE role_name = ?), is_active = ? WHERE user_id = ?";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id WHERE u.user_id = ?";
    
    private static final String SELECT_USER_BY_EMAIL = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id WHERE u.email = ?";
    
    private static final String SELECT_USER_BY_USER_CODE = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id WHERE u.user_code = ?";
    
    private static final String SELECT_ALL_USERS = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id ORDER BY u.created_at DESC";
    
    private static final String SELECT_USERS_BY_ROLE = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id WHERE r.role_name = ? ORDER BY u.created_at DESC";
    
    private static final String SELECT_USERS_BY_ACTIVE = 
        "SELECT u.user_id, u.user_code, u.email, u.password_hash, u.password_salt, u.first_name, u.last_name, u.phone, u.address, " +
        "r.role_name, u.is_active, u.created_at, u.last_login " +
        "FROM user u JOIN role r ON u.role_id = r.role_id WHERE u.is_active = ? ORDER BY u.created_at DESC";
    
    private static final String EXISTS_BY_EMAIL = "SELECT COUNT(*) FROM user WHERE email = ?";
    private static final String EXISTS_BY_USER_CODE = "SELECT COUNT(*) FROM user WHERE user_code = ?";
    private static final String DELETE_USER = "DELETE FROM user WHERE user_id = ?";
    private static final String COUNT_USERS = "SELECT COUNT(*) FROM user";
    private static final String COUNT_USERS_BY_ROLE = 
        "SELECT COUNT(*) FROM user u JOIN role r ON u.role_id = r.role_id WHERE r.role_name = ?";
    private static final String UPDATE_LAST_LOGIN = "UPDATE user SET last_login = ? WHERE user_id = ?";
    
    public SqliteUserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    @Override
    public User save(User user) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection()) {
            if (user.getId() == null) {
                return insert(connection, user);
            } else {
                return update(connection, user);
            }
        } catch (SQLException e) {
            logger.severe("Failed to save user: " + e.getMessage());
            throw new DatabaseException("USER_SAVE", "Failed to save user", e);
        }
    }
    
    private User insert(Connection connection, User user) throws SQLException {
        String userCode = generateUserCode(connection);
        
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_USER)) {
            stmt.setString(1, userCode);
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword().getHashedPassword());
            stmt.setString(4, user.getPassword().getSalt());
            stmt.setString(5, user.getFirstName());
            stmt.setString(6, user.getLastName());
            stmt.setString(7, user.getPhone() != null ? user.getPhone() : "");
            stmt.setString(8, user.getAddress() != null ? user.getAddress() : "");
            stmt.setString(9, user.getRole().name());
            stmt.setBoolean(10, user.isActive());
            stmt.setString(11, LocalDateTime.now().toString());
            
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to insert user, no rows affected");
            }
            
            // Query for the generated ID using last_insert_rowid()
            try (PreparedStatement idStmt = connection.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet rs = idStmt.executeQuery()) {
                
                if (rs.next()) {
                    Long userId = rs.getLong(1);
                    return new User(userId, new Username(userCode), user.getPassword(), 
                                  user.getFirstName(), user.getLastName(), user.getEmail(), 
                                  user.getPhone(), user.getAddress(), user.getRole(), user.isActive(), 
                                  user.getCreatedAt(), user.getUpdatedAt(), user.getLastLoginAt());
                } else {
                    throw new SQLException("Failed to get generated user ID");
                }
            }
        }
    }
    
    private User update(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_USER)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword().getHashedPassword());
            stmt.setString(3, user.getPassword().getSalt());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getPhone() != null ? user.getPhone() : "");
            stmt.setString(7, user.getAddress() != null ? user.getAddress() : "");
            stmt.setString(8, user.getRole().name());
            stmt.setBoolean(9, user.isActive());
            stmt.setLong(10, user.getId());
            
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to update user, no rows affected");
            }
            
            return user;
        }
    }
    
    @Override
    public Optional<User> findById(Long id) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_USER_BY_ID)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Failed to find user by ID: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find user by ID", e);
        }
    }
    
    @Override
    public Optional<User> findByUsername(Username username) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_USER_BY_USER_CODE)) {
            
            stmt.setString(1, username.getValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Failed to find user by username: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find user by username", e);
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_USER_BY_EMAIL)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.severe("Failed to find user by email: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find user by email", e);
        }
    }
    
    @Override
    public List<User> findAll() throws DatabaseException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_USERS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            logger.severe("Failed to find all users: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find all users", e);
        }
    }
    
    @Override
    public List<User> findByRole(UserRole role) throws DatabaseException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_USERS_BY_ROLE)) {
            
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find users by role: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find users by role", e);
        }
    }
    
    @Override
    public List<User> findByActive(boolean active) throws DatabaseException {
        List<User> users = new ArrayList<>();
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_USERS_BY_ACTIVE)) {
            
            stmt.setBoolean(1, active);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                return users;
            }
        } catch (SQLException e) {
            logger.severe("Failed to find users by active status: " + e.getMessage());
            throw new DatabaseException("USER_FIND", "Failed to find users by active status", e);
        }
    }
    
    @Override
    public boolean existsByUsername(Username username) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_USER_CODE)) {
            
            stmt.setString(1, username.getValue());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("Failed to check username existence: " + e.getMessage());
            throw new DatabaseException("USER_EXISTS", "Failed to check username existence", e);
        }
    }
    
    @Override
    public boolean existsByEmail(String email) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_EMAIL)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("Failed to check email existence: " + e.getMessage());
            throw new DatabaseException("USER_EXISTS", "Failed to check email existence", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_USER)) {
            
            stmt.setLong(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            logger.severe("Failed to delete user: " + e.getMessage());
            throw new DatabaseException("USER_DELETE", "Failed to delete user", e);
        }
    }
    
    @Override
    public long count() throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_USERS);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.severe("Failed to count users: " + e.getMessage());
            throw new DatabaseException("USER_COUNT", "Failed to count users", e);
        }
    }
    
    @Override
    public long countByRole(UserRole role) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_USERS_BY_ROLE)) {
            
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            logger.severe("Failed to count users by role: " + e.getMessage());
            throw new DatabaseException("USER_COUNT", "Failed to count users by role", e);
        }
    }
    
    @Override
    public boolean updateLastLogin(Long userId) throws DatabaseException {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, userId);
            
            int affected = stmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            logger.severe("Failed to update last login for user ID " + userId + ": " + e.getMessage());
            throw new DatabaseException("USER_UPDATE_LOGIN", "Failed to update last login", e);
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Long id = rs.getLong("user_id");
        String userCode = rs.getString("user_code");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String passwordSalt = rs.getString("password_salt");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String roleName = rs.getString("role_name");
        boolean isActive = rs.getBoolean("is_active");
        String createdAtStr = rs.getString("created_at");
        String lastLoginStr = rs.getString("last_login");
        
        LocalDateTime createdAt = createdAtStr != null ? LocalDateTime.parse(createdAtStr) : LocalDateTime.now();
        LocalDateTime lastLogin = lastLoginStr != null ? LocalDateTime.parse(lastLoginStr) : null;
        
        return new User(
            id,
            new Username(userCode),
            new Password(passwordHash, passwordSalt),
            firstName,
            lastName,
            email,
            phone,
            address,
            UserRole.valueOf(roleName),
            isActive,
            createdAt,
            createdAt, // updatedAt same as createdAt for now
            lastLogin
        );
    }
    
    private String generateUserCode(Connection connection) throws SQLException {
        String prefix = "USR";
        String query = "SELECT user_code FROM user WHERE user_code LIKE ? ORDER BY user_code DESC LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String lastCode = rs.getString("user_code");
                    int lastNumber = Integer.parseInt(lastCode.substring(3));
                    return String.format("%s%05d", prefix, lastNumber + 1);
                }
            }
        }
        return prefix + "00001";
    }
}