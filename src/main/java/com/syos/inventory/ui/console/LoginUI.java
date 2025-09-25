package com.syos.inventory.ui.console;

import com.syos.inventory.application.service.UserService;
import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.value.UserRole;
import com.syos.application.services.ProductManagementServiceFixed;
import com.syos.presentation.ui.OnlineCustomerUI;
import com.syos.inventory.infrastructure.database.DatabaseManager;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Console-based login interface with ASCII art banner
 * Provides a text-based user interface for system authentication
 */
public class LoginUI {
    
    private static final Logger logger = Logger.getLogger(LoginUI.class.getName());
    
    private final UserService userService;
    private final ProductManagementServiceFixed productService;
    private final Scanner scanner;
    private User currentUser;
    
    /**
     * Constructor
     * @param userService User service for authentication
     */
    public LoginUI(UserService userService) {
        this.userService = userService;
        this.productService = new ProductManagementServiceFixed();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Displays the main welcome screen with options
     */
    public void displayLoginScreen() {
        clearScreen();
        displayBanner();
        displayWelcomeMenu();
    }
    
    /**
     * Displays the ASCII art banner for SYOS system
     */
    private void displayBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║   ███████╗ ██╗   ██╗  ██████╗  ███████╗                             ║");
        System.out.println("  ║   ██╔════╝ ╚██╗ ██╔╝ ██╔═══██╗ ██╔════╝                             ║");
        System.out.println("  ║   ███████╗  ╚████╔╝  ██║   ██║ ███████╗                             ║");
        System.out.println("  ║   ╚════██║   ╚██╔╝   ██║   ██║ ╚════██║                             ║");
        System.out.println("  ║   ███████║    ██║    ╚██████╔╝ ███████║                             ║");
        System.out.println("  ║   ╚══════╝    ╚═╝     ╚═════╝  ╚══════╝                             ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║                    INVENTORY MANAGEMENT SYSTEM                      ║");
        System.out.println("  ║                            Version 1.0.0                            ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Displays the welcome menu with options
     */
    private void displayWelcomeMenu() {
        while (true) {
            System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
            System.out.println("  │                           WELCOME MENU                             │");
            System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
            System.out.println();
            System.out.println("  Please select an option:");
            System.out.println("  1. 🔐 Staff/Customer Login");
            System.out.println("  2. 👤 Register (New Customer)");
            System.out.println("  3. 🛒 Guest Shopping (Online Store)");
            System.out.println("  4. 🚪 Exit");
            System.out.println();
            System.out.print("  Enter your choice (1-4): ");
            
            try {
                // Check if scanner has input available
                if (scanner.hasNextLine()) {
                    String choice = scanner.nextLine().trim();
                    
                    switch (choice) {
                        case "1":
                            handleLogin();
                            return;
                        case "2":
                            handleRegistration();
                            break;
                        case "3":
                            handleOnlineStore();
                            break;
                        case "4":
                            exitApplication();
                            return;
                        default:
                            System.out.println("  ⚠️  Invalid choice. Please enter 1, 2, 3, or 4.");
                            System.out.println();
                            break;
                    }
                } else {
                    System.out.println("  ❌ Input stream error. Restarting menu...");
                    System.out.print("  Enter your choice (1-4): ");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("  ❌ Error occurred: " + e.getMessage());
                System.out.println("  Press Enter to continue...");
                try {
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                } catch (Exception ignored) {
                    // Ignore scanner issues during error handling
                }
            }
        }
    }
    
    /**
     * Handles user login process
     */
    private void handleLogin() {
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                              LOGIN                                  │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        int maxAttempts = 3;
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            try {
                // Get email
                System.out.print("  Email: ");
                String email = scanner.nextLine().trim();
                
                if (email.isEmpty()) {
                    System.out.println("  ⚠️  Email cannot be empty. Please try again.");
                    System.out.println();
                    continue;
                }
                
                // Get password
                System.out.print("  Password: ");
                String password = readPassword();
                
                if (password.isEmpty()) {
                    System.out.println("  ⚠️  Password cannot be empty. Please try again.");
                    System.out.println();
                    continue;
                }
                
                // Attempt authentication
                if (authenticateByEmail(email, password)) {
                    displayLoginSuccess();
                    displayMainMenu();
                    return;
                } else {
                    attempts++;
                    int remaining = maxAttempts - attempts;
                    
                    if (remaining > 0) {
                        System.out.println("  ❌ Invalid username or password. " + remaining + " attempts remaining.");
                        System.out.println();
                    } else {
                        displayLoginFailure();
                        return;
                    }
                }
                
            } catch (Exception e) {
                logger.severe("Error during login attempt: " + e.getMessage());
                System.out.println("  ❌ System error occurred during login. Please try again.");
                System.out.println();
            }
        }
    }
    
    /**
     * Handles online store access for customers
     */
    private void handleOnlineStore() {
        clearScreen();
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                        🛒 ONLINE STORE                             │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        try {
            // Get database path from DatabaseManager and construct URL
            String databasePath = DatabaseManager.getInstance().getDatabasePath();
            String databaseUrl = "jdbc:sqlite:" + databasePath;
            
            // Create and start online customer UI
            OnlineCustomerUI onlineUI = new OnlineCustomerUI(scanner, databaseUrl);
            onlineUI.displayOnlineStore();
            
        } catch (Exception e) {
            System.out.println("  ❌ Error accessing online store: " + e.getMessage());
            System.out.println();
            System.out.print("  Press Enter to continue...");
            scanner.nextLine();
        }
    }

    /**
     * Handles customer registration process
     */
    private void handleRegistration() {
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                       CUSTOMER REGISTRATION                        │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        try {
            System.out.print("  First Name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("  Last Name: ");
            String lastName = scanner.nextLine().trim();
            
            System.out.print("  Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("  Password: ");
            String password = readPassword();
            
            System.out.print("  Confirm Password: ");
            String confirmPassword = readPassword();
            
            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("  ⚠️  All fields are required. Please try again.");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                System.out.println("  ⚠️  Passwords do not match. Please try again.");
                return;
            }
            
            // Create customer account
            userService.createUser(generateUserCode(), password, firstName, lastName, email, UserRole.USER);
            
            System.out.println("  ✅ Registration successful! You can now login with your email and password.");
            System.out.println();
            System.out.print("  Press Enter to continue...");
            scanner.nextLine();
            
        } catch (Exception e) {
            System.out.println("  ❌ Registration failed: " + e.getMessage());
            System.out.println();
            System.out.print("  Press Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Generates a unique user code for new customers
     */
    private String generateUserCode() {
        return "CUST" + System.currentTimeMillis() % 100000;
    }
    
    /**
     * Attempts to authenticate user by email with provided credentials
     * @param email User email
     * @param password Password
     * @return true if authentication successful, false otherwise
     */
    private boolean authenticateByEmail(String email, String password) {
        try {
            // We need to get user by email first, then authenticate
            // Since authenticate method uses username, we'll use a different approach
            User user = userService.getUserByEmail(email);
            if (user != null) {
                // Use the user's username (user_code) for authentication
                currentUser = userService.authenticate(user.getUsername().getValue(), password);
                return currentUser != null;
            }
            return false;
        } catch (Exception e) {
            logger.warning("Authentication failed for email: " + email + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Displays login success message
     */
    private void displayLoginSuccess() {
        clearScreen();
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║                        ✅ LOGIN SUCCESSFUL                          ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║   Welcome back, " + String.format("%-15s", currentUser.getFirstName()) + "                                    ║");
        System.out.println("  ║   Role: " + String.format("%-20s", currentUser.getRole().getDisplayName()) + "                                   ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Wait for user to read the message
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Displays login failure message
     */
    private void displayLoginFailure() {
        clearScreen();
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║                         ❌ LOGIN FAILED                            ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║              Maximum login attempts exceeded.                       ║");
        System.out.println("  ║              Please contact your administrator.                     ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Displays the main menu based on user role
     */
    private void displayMainMenu() {
        boolean running = true;
        
        while (running) {
            clearScreen();
            displayMenuHeader();
            
            if (currentUser.hasRole(UserRole.ADMIN)) {
                displayAdminMenu();
            } else if (currentUser.hasRole(UserRole.CASHIER)) {
                displayCashierMenu();
            } else {
                displayUserMenu();
            }
            
            System.out.print("  Select an option: ");
            String choice = "";
            try {
                if (scanner.hasNextLine()) {
                    choice = scanner.nextLine().trim();
                } else {
                    System.out.println("  ❌ Input stream error. Please try again.");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("  ❌ Input error: " + e.getMessage());
                continue;
            }
            
            switch (choice.toLowerCase()) {
                case "1":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleProductManagement();
                    } else if (currentUser.hasRole(UserRole.CASHIER)) {
                        handleMenuOption3(); // Inventory Management for Cashier
                    } else {
                        // For customers, this shouldn't be reached as they go directly to Online Store
                        handleOnlineStore();
                    }
                    break;
                case "2":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleUserManagement();
                    } else if (currentUser.hasRole(UserRole.CASHIER)) {
                        handleMenuOption2(); // Process Sales for Cashier
                    } else {
                        // For customers, this shouldn't be reached as they go directly to Online Store
                        handleOnlineStore();
                    }
                    break;
                case "3":
                    handleMenuOption3();
                    break;
                case "4":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handlePOSTerminal();
                    } else {
                        handleMenuOption4();
                    }
                    break;
                case "5":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleMenuOption5();
                    } else {
                        System.out.println("  ⚠️  Invalid option. Please try again.");
                        pauseForUser();
                    }
                    break;
                case "6":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleMenuOption6();
                    } else {
                        System.out.println("  ⚠️  Invalid option. Please try again.");
                        pauseForUser();
                    }
                    break;
                case "7":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleMenuOption7();
                    } else {
                        System.out.println("  ⚠️  Invalid option. Please try again.");
                        pauseForUser();
                    }
                    break;
                case "8":
                    if (currentUser.hasRole(UserRole.ADMIN)) {
                        handleOnlineStore();
                    } else {
                        System.out.println("  ⚠️  Invalid option. Please try again.");
                        pauseForUser();
                    }
                    break;
                case "q":
                case "quit":
                case "exit":
                    running = false;
                    displayLogout();
                    break;
                default:
                    System.out.println("  ⚠️  Invalid option. Please try again.");
                    pauseForUser();
                    break;
            }
        }
    }
    
    /**
     * Displays the menu header
     */
    private void displayMenuHeader() {
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                              MAIN MENU                              ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║   User: " + String.format("%-25s", currentUser.getUsername().getValue()) + " Role: " + String.format("%-15s", currentUser.getRole().getDisplayName()) + "   ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
        
        // Display reorder alerts for admins
        if (currentUser.hasRole(UserRole.ADMIN)) {
            displayReorderAlerts();
        }
        
        System.out.println();
    }
    
    /**
     * Displays reorder alerts if any products need restocking
     */
    private void displayReorderAlerts() {
        try {
            var alerts = productService.getReorderAlerts();
            if (!alerts.isEmpty()) {
                System.out.println();
                System.out.println("  ⚠️  INVENTORY ALERTS ⚠️");
                System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
                
                for (String alert : alerts) {
                    String[] parts = alert.split(" - ");
                    if (parts.length >= 2) {
                        String productInfo = parts[0].replace("REORDER ALERT: ", "");
                        String quantities = parts[1];
                        
                        System.out.println("  ║ 🔴 " + String.format("%-64s", productInfo) + " ║");
                        System.out.println("  ║    " + String.format("%-64s", quantities) + " ║");
                    }
                }
                
                System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
            }
        } catch (Exception e) {
            // Don't show alerts if there's an error, just log it
            System.err.println("Warning: Could not load inventory alerts");
        }
    }
    
    /**
     * Displays admin menu options
     */
    private void displayAdminMenu() {
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                         ADMIN MENU                                 │");
        System.out.println("  ├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("  │  1. Product Management                                              │");
        System.out.println("  │  2. User Management                                                 │");
        System.out.println("  │  3. Inventory Management                                            │");
        System.out.println("  │  4. POS Terminal                                                    │");
        System.out.println("  │  5. Reports & Analytics                                             │");
        System.out.println("  │  6. System Configuration                                            │");
        System.out.println("  │  7. Audit Logs                                                     │");
        System.out.println("  │  8. 🛒 Online Store (Customer View)                                │");
        System.out.println("  │                                                                     │");
        System.out.println("  │  Q. Quit                                                            │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }
    
    /**
     * Displays cashier menu options
     */
    private void displayCashierMenu() {
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                        CASHIER MENU                                │");
        System.out.println("  ├─────────────────────────────────────────────────────────────────────┤");
        System.out.println("  │  1. Inventory Management                                            │");
        System.out.println("  │  2. Process Sales                                                   │");
        System.out.println("  │  3. View Products                                                   │");
        System.out.println("  │                                                                     │");
        System.out.println("  │  Q. Quit                                                            │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }
    
    /**
     * Displays user menu options - redirects customers directly to Online Store
     */
    private void displayUserMenu() {
        // For customers, redirect directly to Online Store
        System.out.println("  ┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("  │                     CUSTOMER ACCESS                                │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Welcome to SYOS Online Store! Redirecting you to shopping...");
        System.out.println();
        
        try {
            Thread.sleep(1500); // Brief pause for user to read message
            handleOnlineStore();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Displays logout message
     */
    private void displayLogout() {
        clearScreen();
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║                         👋 GOODBYE                                  ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ║                    Thank you for using SYOS                         ║");
        System.out.println("  ║                     Inventory Management System                     ║");
        System.out.println("  ║                                                                      ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Reads password input (simple implementation)
     * Note: In a real application, you might want to use Console.readPassword() for better security
     */
    private String readPassword() {
        return scanner.nextLine().trim();
    }
    
    /**
     * Clears the console screen
     */
    private void clearScreen() {
        try {
            // For Windows
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/Mac
                System.out.print("\033[2J\033[H");
            }
        } catch (Exception e) {
            // Fallback: print empty lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Handle product management for admin users
     */
    private void handleProductManagement() {
        try {
            // Use the existing productService field
            ProductManagementUI productUI = new ProductManagementUI(scanner, currentUser, productService);
            productUI.displayProductManagement();
        } catch (Exception e) {
            System.out.println("  ⚠️  Error accessing product management: " + e.getMessage());
            pauseForUser();
        }
    }
    
    /**
     * Handle user management for admin users
     */
    private void handleUserManagement() {
        try {
            UserManagementUI userManagementUI = new UserManagementUI(scanner, currentUser, userService);
            userManagementUI.displayMenu();
        } catch (Exception e) {
            System.err.println("❌ Error accessing user management: " + e.getMessage());
            logger.severe("Error in user management: " + e.getMessage());
            pauseForUser();
        }
    }
    
    /**
     * Handle menu option 1 for non-admin users
     */
    private void handleMenuOption1() {
        System.out.println("  📦 Inventory viewing feature coming soon...");
        pauseForUser();
    }
    
    /**
     * Handle menu option 2 for non-admin users  
     */
    private void handleMenuOption2() {
        System.out.println("  💰 Sales processing feature coming soon...");
        pauseForUser();
    }
    
    /**
     * Handle menu option 3 - Inventory Management
     */
    private void handleMenuOption3() {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            try {
                InventoryManagementUI inventoryUI = new InventoryManagementUI(scanner, currentUser);
                inventoryUI.start();
            } catch (Exception e) {
                System.out.println("  ❌ Error accessing Inventory Management: " + e.getMessage());
                pauseForUser();
            }
        } else {
            System.out.println("  📋 Product viewing feature coming soon...");
            pauseForUser();
        }
    }
    
    /**
     * Handle menu option 4
     */
    private void handleMenuOption4() {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            System.out.println("  📈 Reports & Analytics feature coming soon...");
        } else {
            System.out.println("  ⚠️  Invalid option. Please try again.");
        }
        pauseForUser();
    }
    
    /**
     * Handle menu option 5 for admin users
     */
    private void handleMenuOption5() {
        System.out.println("  ⚙️  System Configuration feature coming soon...");
        pauseForUser();
    }
    
    /**
     * Handle menu option 6 for admin users
     */
    private void handleMenuOption6() {
        System.out.println("  ⚙️  System Configuration feature coming soon...");
        pauseForUser();
    }
    
    /**
     * Handle menu option 7 for admin users (Audit Logs)
     */
    private void handleMenuOption7() {
        System.out.println("  📝 Audit Logs feature coming soon...");
        pauseForUser();
    }
    
    /**
     * Handle POS Terminal access
     */
    private void handlePOSTerminal() {
        try {
            POSTerminalUI posTerminal = new POSTerminalUI(scanner, currentUser);
            posTerminal.start();
        } catch (Exception e) {
            System.out.println("  ❌ Error accessing POS Terminal: " + e.getMessage());
            pauseForUser();
        }
    }
    
    /**
     * Pauses execution and waits for user input
     */
    private void pauseForUser() {
        System.out.print("  Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Gets the currently logged-in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
        logger.info("User logged out successfully");
    }
    
    /**
     * Main entry point for the login UI
     * Starts the login process
     */
    public void start() {
        logger.info("Starting SYOS Login UI");
        displayLoginScreen();
    }

    /**
     * Properly exits the application with terminal cleanup
     */
    private void exitApplication() {
        try {
            // Display goodbye message without clearing screen
            System.out.println();
            System.out.println("  ╔══════════════════════════════════════════════════════════════════════╗");
            System.out.println("  ║                                                                      ║");
            System.out.println("  ║                         👋 GOODBYE                                  ║");
            System.out.println("  ║                                                                      ║");
            System.out.println("  ║                    Thank you for using SYOS                         ║");
            System.out.println("  ║                     Inventory Management System                     ║");
            System.out.println("  ║                                                                      ║");
            System.out.println("  ╚══════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            
            // Close scanner properly
            if (scanner != null) {
                scanner.close();
            }
            
            // Reset terminal cursor and ensure proper cleanup
            System.out.print("\033[0m"); // Reset all attributes
            System.out.print("\033[?25h"); // Show cursor
            System.out.println(); // Final newline
            System.out.flush(); // Flush output
            
        } catch (Exception e) {
            logger.warning("Error during application exit: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }
}