package edu.eci.arsw.partII.blacklistvalidator;

import java.util.List;

/**
 * Entry point for optimized blacklist validation demonstration.
 *
 * Demonstrates the enhanced functionality of HostBlackListsValidator using
 * early termination optimization with race condition prevention.
 *
 * @author David Velásquez, Jesús Pinzón
 * @version 2.0
 * @since 2025-09-03
 */
public class Main {

    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        HostBlackListsValidator validator = new HostBlackListsValidator();

        System.out.println("OPTIMIZED BLACKLIST VALIDATOR DEMO");
        System.out.println("=====================================\n");

        // Test case 1: IP that should trigger early termination
        System.out.println("TEST CASE 1: Early Termination Demo");
        System.out.println("Testing IP: 202.24.34.55 (should reach threshold quickly)");
        System.out.println(repeat("-", 60));

        validator.checkHostOptimized("202.24.34.55", 4);

        System.out.println("\n" + repeat("=", 80) + "\n");

        // Test case 2: IP that might not reach threshold
        System.out.println("TEST CASE 2: Standard Execution Demo");
        System.out.println("Testing IP: 200.24.34.55 (may not reach threshold)");
        System.out.println(repeat("-", 60));

        validator.checkHostOptimized("200.24.34.55", 4);

    }
}
