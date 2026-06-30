package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ticket{
    public enum Priority{
        LOW, MEDIUM, HIGH;
        public static Priority fromChoice(int choice) {return values()[choice - 1];}
    }
    public enum Status{
        OPEN, IN_PROGRESS, RESOLVED, CLOSED;
        public static Status fromChoice(int choice) {return values()[choice - 1];}
    }

    public static final String[] CATEGORIES = {"Billing", "Network", "Technical", "Complaint", "General Inquiry"};
    
    private final String ticketID;
    private final String customerName;
    private final String phoneNumber;
    private String category;
    private Priority priority;
    private Status status;
    private String description;
    private final LocalDate creationDate;
    private final List<String> comments;

    //new ticket
    public Ticket(String ticketID, String customerName, String phoneNumber, String category, Priority priority, String description){
        this.ticketID = ticketID;
        this.customerName = customerName;
        this.phoneNumber=phoneNumber;
        this.category=category;
        this.priority=priority;
        this.status = Status.OPEN;
        this.description=description;
        this.creationDate = LocalDate.now();
        this.comments = new ArrayList<>();
    }

    //load from file
    public Ticket(String ticketID, String customerName, String phoneNumber, String category, Priority priority, Status status, String description, LocalDate creationDate, List<String> comments){
        this.ticketID = ticketID;
        this.customerName = customerName;
        this.phoneNumber=phoneNumber;
        this.category=category;
        this.priority=priority;
        this.status = status;
        this.description=description;
        this.creationDate = creationDate;
        this.comments = comments != null ? comments : new ArrayList<>();
    } 

    //getters
    public String getTicketID(){return ticketID;}
    public String getCustomerName() {return customerName;}
    public String getPhoneNumber()  { return phoneNumber; }
    public String getCategory()     { return category; }
    public Priority getPriority()     { return priority; }
    public Status getStatus()       { return status; }
    public String getDescription()  { return description; }
    public LocalDate getCreationDate() { return creationDate; }
    public List<String> getComments()     { return comments; }

    //setters
    public void setPriority(Priority priority){ this.priority = priority; }
    public void setStatus(Status status){ this.status = status; }
    public void setDescription(String description){ this.description = description; }

    public void addComment(String text) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        comments.add(timestamp + ": " + text);
    }

    public String toFileLine() {
        String commentsField = String.join("\u001F", comments);
        return String.join("\t", ticketID, customerName, phoneNumber, category, priority.name(),status.name(), description, creationDate.toString(),commentsField);
    } ////priority.name() and status.name() convert the enum constants back to their string names ("HIGH", "OPEN", etc.) for storage.

    public static Ticket fromFileLine(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Corrupted ticket line: " + line);
        }
        List<String> comments = new ArrayList<>();
        if (!parts[8].isBlank()) {
            for (String c : parts[8].split("\u001F", -1)) {
                comments.add(c);
            }
        }
        return new Ticket(parts[0], parts[1], parts[2], parts[3], Priority.valueOf(parts[4]), Status.valueOf(parts[5]), parts[6], LocalDate.parse(parts[7]), comments);
    }

    //view all
    public String toTableRow() {
        return String.format("%-6s  %-20s  %-16s  %-8s  %-11s",
            ticketID,
            truncate(customerName, 20),
            category,
            priority.name(),
            status.name()
        );
    }

    public void displayDetails() {
        String line = "─".repeat(50);
        System.out.println(line);
        System.out.printf("  Ticket ID    : %s%n", ticketID);
        System.out.printf("  Customer     : %s%n", customerName);
        System.out.printf("  Phone        : %s%n", phoneNumber);
        System.out.printf("  Category     : %s%n", category);
        System.out.printf("  Priority     : %s%n", priority.name());
        System.out.printf("  Status       : %s%n", status.name());
        System.out.printf("  Description  : %s%n", description);
        System.out.printf("  Created      : %s%n", creationDate);
        System.out.println("  Comments     :");
        if (comments.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (String c : comments) {
                System.out.println("    • " + c);
            }
        }
        System.out.println(line);
    }

    @Override
    public String toString() {
        return "Ticket{id=" + ticketID + ", customer=" + customerName + ", status=" + status.name() + ", priority=" + priority.name() + "}";
    }
    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}