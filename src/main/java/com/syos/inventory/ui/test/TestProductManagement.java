package com.syos.inventory.ui.test;

import com.syos.inventory.ui.console.ProductManagementUI;
import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.value.Username;
import com.syos.inventory.domain.value.Password;
import com.syos.inventory.domain.value.UserRole;
import com.syos.application.services.ProductManagementServiceFixed;

import java.util.Scanner;

/**
 * Simple test runner for Product Management UI
 */
public class TestProductManagement {
    public static void main(String[] args) {
        System.out.println("=== SYOS Product Management System Test ===");
        System.out.println("============================================");
        
        try {
            // Create a test admin user
            Username adminUsername = new Username("admin");
            Password adminPassword = new Password("admin123");
            User adminUser = new User(adminUsername, adminPassword, "Admin", "User", 
                                    "admin@syos.com", UserRole.ADMIN);
            
            // Create scanner and ProductManagementUI with service
            Scanner scanner = new Scanner(System.in);
            ProductManagementServiceFixed productService = new ProductManagementServiceFixed();
            ProductManagementUI productUI = new ProductManagementUI(scanner, adminUser, productService);
            
            System.out.println("Testing with Admin User: " + adminUser.getFullName());
            System.out.println("Role: " + adminUser.getRole().getDisplayName());
            System.out.println("============================================\n");
            
            // Launch the product management interface
            productUI.displayProductManagement();
            
            System.out.println("\n=== Thank you for testing SYOS Product Management ===");
            
        } catch (Exception e) {
            System.err.println("Error running Product Management test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}