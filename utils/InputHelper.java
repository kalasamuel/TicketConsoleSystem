package utils;

import java.util.Scanner;

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String getOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int getMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) return choice;
                System.out.printf("  Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a number.");
            }
        }
    }

    public boolean getConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("yes") || response.equals("y")) return true;
            if (response.equals("no")  || response.equals("n")) return false;
            System.out.println("  [!] Please type yes or no.");
        }
    }
}