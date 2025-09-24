package com.syos.inventory.domain.value;

/**
 * Enumeration of user roles in the system
 */
public enum UserRole {
    ADMIN("Administrator", "Full system access with user management capabilities"),
    CASHIER("Cashier", "Access to sales transactions and basic inventory viewing"),
    USER("Customer", "Online customer with basic access to view products and place orders");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Get the display name for the role
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the description for the role
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this role has admin privileges
     * @return true if admin role
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role has admin privileges
     * @return true if admin role
     */
    public boolean isAdminRole() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role can manage users
     * @return true if role can manage users
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role can access reports
     * @return true if role can access reports
     */
    public boolean canAccessReports() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role can manage inventory
     * @return true if role can manage inventory
     */
    public boolean canManageInventory() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role can process sales
     * @return true if role can process sales
     */
    public boolean canProcessSales() {
        return this != USER; // Only employees (not customers) can process sales
    }
    
    /**
     * Check if this role can view products (for customers)
     * @return true if role can view products
     */
    public boolean canViewProducts() {
        return true; // All roles can view products
    }
    
    /**
     * Check if this role can place orders (for customers)
     * @return true if role can place orders
     */
    public boolean canPlaceOrders() {
        return this == USER || this == ADMIN; // Customers and staff can place orders
    }
    
    /**
     * Parse role from string
     * @param roleName Role name
     * @return UserRole enum
     * @throws IllegalArgumentException if role not found
     */
    public static UserRole fromString(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        
        try {
            return UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }
    }
}