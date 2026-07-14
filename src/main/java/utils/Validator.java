package utils;

import models.Ticket;
import models.Ticket.Priority;
import models.Ticket.Status;

public class Validator {

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
    public static boolean isValidDescription(String value) {
        return value != null && value.trim().length() >= 5;
    }
    public static String descriptionError() {
        return "Description must be at least 5 characters.";
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String stripped = phone.replaceAll("\\s+", "");
        return stripped.matches("^(07\\d{8}|\\+2567\\d{8})$");
    }
    public static String phoneError() {
        return "Phone must be 07XXXXXXXX or +2567XXXXXXXX.";
    }

    public static String categoryFromChoice(int choice) {
        return Ticket.CATEGORIES[choice - 1];
    }

    public static Priority priorityFromChoice(int choice) {
        return Priority.fromChoice(choice);
    }
    public static Status statusFromChoice(int choice) {
        return Status.fromChoice(choice);
    }
}
