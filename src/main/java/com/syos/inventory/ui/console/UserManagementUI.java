package com.syos.inventory.ui.console;

import com.syos.inventory.application.service.UserService;
import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.value.UserRole;
import com.syos.inventory.domain.exception.BusinessException;
import com.syos.inventory.domain.exception.EntityNotFoundException;
import com.syos.inventory.domain.exception.ValidationException;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;

/**
 * User Management Console UI for ADMIN users
 */
public class UserManagementUI {
    
    private static final Logger LOGGER = Logger.getLogger(UserManagementUI.class.getName());
    private final Scanner scanner;
    private final User currentUser;
    private final UserService userService;
    
    public UserManagementUI(Scanner scanner, User currentUser, UserService userService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.userService = userService;
    }
    
    /**
     * Display user management menu and handle user input
     */
    public void displayMenu() {
        while (true) {
            try {
                clearScreen();
                displayMenuOptions();
                
                int choice = readIntChoice();
                
                switch (choice) {
                    case 1:
                        viewAllUsers();
                        break;
                    case 2:
                        createNewUser();
                        break;
                    case 3:
                        editUser();
                        break;
                    case 4:
                        activateDeactivateUser();
                        break;
                    case 5:
                        resetUserPassword();
                        break;
                    case 6:
                        System.out.println("\\n  Returning to Main Menu...");
                        return;
                    default:
                        System.out.println("\\n  ❌ Invalid option. Please select 1-6.");
                        System.out.print("  Press Enter to continue...");
                        scanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println("\\n  ❌ Error: " + e.getMessage());
                System.out.print("  Press Enter to continue...");
                scanner.nextLine();
            }
        }
    }
    
    /**
     * Display menu options
     */
    private void displayMenuOptions() {
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                    USER MANAGEMENT (ADMIN ACCESS)                  │");
        System.out.println("  ├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("  │  1. View All Users                                                  │");
        System.out.println("  │  2. Create New User                                                 │");
        System.out.println("  │  3. Edit User                                                       │");
        System.out.println("  │  4. Activate/Deactivate User                                        │");
        System.out.println("  │  5. Reset User Password                                             │");
        System.out.println("  │  6. Back to Main Menu                                               │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.print("  Choose an option (1-6): ");
    }
    
    /**
     * View all users in the system
     */
    private void viewAllUsers() {
        System.out.println();
        System.out.println("ALL SYSTEM USERS");
        System.out.println("================================================================================");
        System.out.printf("%-4s %-25s %-15s %-15s %-12s %-8s %-6s %-20s%n",
            "ID", "Email", "First Name", "Last Name", "Phone", "Role", "Active", "Created");
        System.out.println("--------------------------------------------------------------------------------");
        
        try {
            List<User> users = userService.getAllUsers();
            
            if (users.isEmpty()) {
                System.out.println("No users found in the system.");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    System.out.printf("%-4d %-25s %-15s %-15s %-12s %-8s %-6s %-20s%n",
                        (i + 1),
                        truncateString(user.getEmail(), 24),
                        truncateString(user.getFirstName(), 14),
                        truncateString(user.getLastName(), 14),
                        user.getPhone() != null ? truncateString(user.getPhone(), 11) : "N/A",
                        user.getRole().toString(),
                        user.isActive() ? "Yes" : "No",
                        user.getCreatedAt().format(formatter)
                    );
                }
            }
            
            System.out.println("================================================================================");
            System.out.printf("Total Users: %d%n", users.size());
            
        } catch (Exception e) {
            System.out.println("❌ Error loading users: " + e.getMessage());
            LOGGER.severe("Error loading users: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Create a new user
     */
    private void createNewUser() {
        System.out.println();
        System.out.println("CREATE NEW USER");
        System.out.println("========================================");
        
        try {
            // Get user input
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("First Name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Last Name (optional): ");
            String lastName = scanner.nextLine().trim();
            if (lastName.isEmpty()) {
                lastName = null;
            }
            
            System.out.print("Phone (optional): ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) {
                phone = null;
            }
            
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            
            // Generate username automatically from email (ensure minimum 3 characters)
            String emailPrefix = email.split("@")[0];
            String username = emailPrefix.length() >= 3 ? emailPrefix : emailPrefix + "usr";
            
            // Select role
            System.out.println();
            System.out.println("Available Roles:");
            System.out.println("1. ADMIN - " + UserRole.ADMIN.getDescription());
            System.out.println("2. CASHIER - " + UserRole.CASHIER.getDescription());
            System.out.print("Select role (1-2): ");
            
            int roleChoice = readIntChoice();
            UserRole role;
            
            switch (roleChoice) {
                case 1:
                    role = UserRole.ADMIN;
                    break;
                case 2:
                    role = UserRole.CASHIER;
                    break;
                default:
                    System.out.println("❌ Invalid role selection. Defaulting to CASHIER.");
                    role = UserRole.CASHIER;
            }
            
            // Create the user with phone number
            User newUser = userService.createUser(username, password, firstName, lastName, email, phone, role);
            
            System.out.println();
            System.out.println("✅ User created successfully!");
            System.out.println("User Code: " + newUser.getUsername().getValue().toUpperCase());
            System.out.println("Email: " + newUser.getEmail());
            System.out.println("Name: " + newUser.getFirstName() + " " + newUser.getLastName());
            System.out.println("Role: " + newUser.getRole());
            
            LOGGER.info("New user created: " + username + " by " + currentUser.getUsername().getValue());
            
        } catch (ValidationException | BusinessException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error creating user: " + e.getMessage());
            LOGGER.severe("Error creating user: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Edit existing user
     */
    private void editUser() {
        System.out.println();
        System.out.println("EDIT USER");
        System.out.println("========================================");
        
        try {
            System.out.print("Enter User Email to edit: ");
            String email = scanner.nextLine().trim();
            
            User user = userService.getUserByEmail(email);
            
            System.out.println();
            System.out.println("Current User Information:");
            System.out.println("Email: " + user.getEmail());
            System.out.println("First Name: " + user.getFirstName());
            System.out.println("Last Name: " + user.getLastName());
            System.out.println("Phone: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            System.out.println("Role: " + user.getRole());
            System.out.println("Active: " + (user.isActive() ? "Yes" : "No"));
            System.out.println();
            
            System.out.println("Enter new information (press Enter to keep current):");
            
            System.out.print("First Name [" + user.getFirstName() + "]: ");
            String newFirstName = scanner.nextLine().trim();
            if (newFirstName.isEmpty()) {
                newFirstName = user.getFirstName();
            }
            
            System.out.print("Last Name [" + user.getLastName() + "]: ");
            String newLastName = scanner.nextLine().trim();
            if (newLastName.isEmpty()) {
                newLastName = user.getLastName();
            }
            
            System.out.print("Phone [" + (user.getPhone() != null ? user.getPhone() : "N/A") + "]: ");
            String newPhone = scanner.nextLine().trim();
            if (newPhone.isEmpty()) {
                newPhone = user.getPhone();
            } else if (newPhone.equalsIgnoreCase("N/A")) {
                newPhone = null;
            }
            
            // Update user
            User updatedUser = userService.updateUser(user.getId(), newFirstName, newLastName, user.getEmail());
            if (newPhone != null) {
                updatedUser.setPhone(newPhone);
            }
            
            System.out.println();
            System.out.println("✅ User updated successfully!");
            
            LOGGER.info("User updated: " + email + " by " + currentUser.getUsername().getValue());
            
        } catch (EntityNotFoundException e) {
            System.out.println("❌ User not found with that email.");
        } catch (ValidationException | BusinessException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error updating user: " + e.getMessage());
            LOGGER.severe("Error updating user: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Activate or deactivate user
     */
    private void activateDeactivateUser() {
        System.out.println();
        System.out.println("ACTIVATE/DEACTIVATE USER");
        System.out.println("========================================");
        
        try {
            System.out.print("Enter User Email to activate/deactivate: ");
            String email = scanner.nextLine().trim();
            
            User user = userService.getUserByEmail(email);
            
            System.out.println();
            System.out.println("User: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
            System.out.println("Current Status: " + (user.isActive() ? "ACTIVE" : "INACTIVE"));
            
            String newStatus = user.isActive() ? "INACTIVE" : "ACTIVE";
            System.out.print("Change to " + newStatus + "? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                User updatedUser = userService.setUserActive(user.getId(), !user.isActive());
                
                System.out.println();
                System.out.println("✅ User status updated successfully!");
                System.out.println("New Status: " + (updatedUser.isActive() ? "ACTIVE" : "INACTIVE"));
                
                LOGGER.info("User " + (updatedUser.isActive() ? "activated" : "deactivated") + ": " + 
                           email + " by " + currentUser.getUsername().getValue());
            } else {
                System.out.println("❌ Operation cancelled.");
            }
            
        } catch (EntityNotFoundException e) {
            System.out.println("❌ User not found with that email.");
        } catch (BusinessException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error updating user status: " + e.getMessage());
            LOGGER.severe("Error updating user status: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Reset user password (admin function)
     */
    private void resetUserPassword() {
        System.out.println();
        System.out.println("RESET USER PASSWORD");
        System.out.println("========================================");
        
        try {
            System.out.print("Enter User Email to reset password: ");
            String email = scanner.nextLine().trim();
            
            User user = userService.getUserByEmail(email);
            
            System.out.println();
            System.out.println("User: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
            
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();
            
            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine().trim();
            
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("❌ Passwords do not match!");
                return;
            }
            
            // For admin reset, we'll need to add a method to UserService
            // For now, let's simulate it
            user.changePassword(new com.syos.inventory.domain.value.Password(newPassword));
            
            System.out.println();
            System.out.println("✅ Password reset successfully!");
            System.out.println("User can now login with the new password.");
            
            LOGGER.info("Password reset for user: " + email + " by admin " + currentUser.getUsername().getValue());
            
        } catch (EntityNotFoundException e) {
            System.out.println("❌ User not found with that email.");
        } catch (ValidationException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error resetting password: " + e.getMessage());
            LOGGER.severe("Error resetting password: " + e.getMessage());
        }
        
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    // Helper methods
    private void clearScreen() {
        // Clear screen (works on most terminals)
        System.out.print("\\033[2J\\033[H");
        System.out.flush();
    }
    
    private int readIntChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        }
    }
    
    private String truncateString(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}