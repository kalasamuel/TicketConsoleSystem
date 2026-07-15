package models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    public enum Priority {
        LOW, MEDIUM, HIGH;
        public static Priority fromChoice(int choice) { return values()[choice - 1]; }
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED;
        public static Status fromChoice(int choice) { return values()[choice - 1]; }
    }

    public static final String[] CATEGORIES = {"Billing", "Network", "Technical", "Complaint", "General Inquiry"};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ticket_code", unique = true, length = 10)
    private String ticketCode;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDate creationDate;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    // builds entities via reflection before populating fields.
    protected Ticket() {}

    // new ticket
    public Ticket(String customerName, String phoneNumber, String category, Priority priority, String description) {
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.category = category;
        this.priority = priority;
        this.status = Status.OPEN;
        this.description = description;
        this.creationDate = LocalDate.now();
    }

    public void assignTicketCode() {
        if (this.ticketCode == null && this.id != null) {
            this.ticketCode = "T" + String.format("%06d", id);
        }
    }

    // getters
    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCategory() { return category; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public String getDescription() { return description; }
    public LocalDate getCreationDate() { return creationDate; }
    public List<Comment> getComments() { return comments; }

    // setters
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setStatus(Status status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }

    public void addComment(String text) {
        Comment comment = new Comment(text, this);
        comments.add(comment);
    }

    // view all
    public String toTableRow() {
        return String.format("%-8s  %-20s  %-16s  %-8s  %-11s",
            ticketCode,
            truncate(customerName, 20),
            category,
            priority.name(),
            status.name()
        );
    }

    public void displayDetails() {
        String line = "─".repeat(50);
        System.out.println(line);
        System.out.printf("  Ticket ID    : %s%n", ticketCode);
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
            for (Comment c : comments) {
                System.out.println("    • " + c.formatted());
            }
        }
        System.out.println(line);
    }

    @Override
    public String toString() {
        return "Ticket{id=" + ticketCode + ", customer=" + customerName + ", status=" + status.name() + ", priority=" + priority.name() + "}";
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
