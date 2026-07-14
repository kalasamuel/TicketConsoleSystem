package services;

import models.Ticket;
import models.Ticket.Priority;
import models.Ticket.Status;
import repository.TicketRepository;
import utils.InputHelper;
import utils.Validator;

import java.util.List;
import java.util.Optional;

public class TicketService {

    private final TicketRepository repository;
    private final InputHelper input;
    private final ReportService reporter;

    public TicketService(TicketRepository repository, InputHelper input) {
        this.repository = repository;
        this.input = input;
        this.reporter = new ReportService();
    }

    // ticket creation
    public void createTicket() {
        System.out.println("\n── Create New Ticket ──────────────────────────");

        String name;
        while (true) {
            name = input.getString("  Customer Name   : ");
            if (Validator.isNotBlank(name)) break;
            System.out.println("  ! Name cannot be blank.");
        }

        String phone;
        while (true) {
            phone = input.getString("  Phone Number    : ");
            if (Validator.isValidPhone(phone)) break;
            System.out.println("  ! " + Validator.phoneError());
        }

        printCategoryMenu();
        int catChoice = input.getMenuChoice("  Select Category : ", 1, Ticket.CATEGORIES.length);
        String category = Validator.categoryFromChoice(catChoice);

        printPriorityMenu();
        int priChoice = input.getMenuChoice("  Select Priority : ", 1, Priority.values().length);
        Priority priority = Validator.priorityFromChoice(priChoice);

        String description;
        while (true) {
            description = input.getString("  Description     : ");
            if (Validator.isValidDescription(description)) break;
            System.out.println("  [!] " + Validator.descriptionError());
        }

        Ticket ticket = new Ticket(name, phone, category, priority, description);
        repository.save(ticket);

        System.out.println("\n   Ticket created successfully.");
        System.out.printf("    Ticket ID : %s | Status : OPEN | Date : %s%n%n",
            ticket.getTicketCode(), ticket.getCreationDate());
    }

    // retrieving all tickets
    public void viewAllTickets() {
        System.out.println("\n── All Tickets ────────────────────────────────");

        List<Ticket> tickets = repository.findAll();
        if (tickets.isEmpty()) {
            System.out.println("  No tickets found.");
            return;
        }

        String header = String.format("%-8s  %-20s  %-16s  %-8s  %-11s",
            "ID", "Customer", "Category", "Priority", "Status");
        String rule = "─".repeat(header.length());

        System.out.println("  " + rule);
        System.out.println("  " + header);
        System.out.println("  " + rule);
        for (Ticket t : tickets) {
            System.out.println("  " + t.toTableRow());
        }
        System.out.println("  " + rule);
        System.out.printf("  %d ticket(s) total.%n%n", tickets.size());
    }

    // retrieving ticket details
    public void viewTicketDetails() {
        System.out.println("\n── View Ticket Details ────────────────────────");
        String code = input.getString("  Enter Ticket ID : ").toUpperCase();

        Optional<Ticket> ticket = repository.findByCode(code);
        if (ticket.isEmpty()) {
            System.out.println("  ! Ticket " + code + " not found.");
            return;
        }
        ticket.get().displayDetails();
    }

    // updating a ticket
    public void updateTicket() {
        System.out.println("\n── Update Ticket ──────────────────────────────");
        String code = input.getString("  Enter Ticket ID : ").toUpperCase();

        Optional<Ticket> found = repository.findByCode(code);
        if (found.isEmpty()) {
            System.out.println("  ! Ticket " + code + " not found.");
            return;
        }
        Ticket ticket = found.get();
        ticket.displayDetails();

        System.out.println("  What would you like to update?");
        System.out.println("    1. Priority");
        System.out.println("    2. Status");
        System.out.println("    3. Add Comment");
        System.out.println("    4. Description");
        System.out.println("    5. Cancel");

        int choice = input.getMenuChoice("  Select option   : ", 1, 5);

        switch (choice) {
            case 1 -> updatePriority(ticket);
            case 2 -> updateStatus(ticket);
            case 3 -> addComment(ticket);
            case 4 -> updateDescription(ticket);
            case 5 -> { System.out.println("  Update cancelled."); return; }
        }

        repository.update(ticket);
        System.out.printf("  Ticket %s updated successfully.%n%n", code);
    }

    private void updatePriority(Ticket ticket) {
        printPriorityMenu();
        int choice = input.getMenuChoice("  Select new priority : ", 1, Priority.values().length);
        ticket.setPriority(Validator.priorityFromChoice(choice));
    }

    private void updateStatus(Ticket ticket) {
        printStatusMenu();
        int choice = input.getMenuChoice("  Select new status : ", 1, Status.values().length);
        Status newStatus = Validator.statusFromChoice(choice);
        ticket.setStatus(newStatus);

        if (newStatus == Status.RESOLVED) {
            String note = input.getOptionalString("  Resolution note (optional, press Enter to skip) : ");
            if (!note.isBlank()) {
                ticket.addComment("[RESOLVED] " + note);
            }
        }
    }

    private void addComment(Ticket ticket) {
        String comment = input.getString("  Enter comment : ");
        ticket.addComment(comment);
    }

    private void updateDescription(Ticket ticket) {
        String desc;
        while (true) {
            desc = input.getString("  New description : ");
            if (Validator.isValidDescription(desc)) break;
            System.out.println("  ! " + Validator.descriptionError());
        }
        ticket.setDescription(desc);
    }

    // delete
    public void deleteTicket() {
        System.out.println("\n── Delete Ticket ──────────────────────────────");
        String code = input.getString("  Enter Ticket ID : ").toUpperCase();

        Optional<Ticket> found = repository.findByCode(code);
        if (found.isEmpty()) {
            System.out.println("  ! Ticket " + code + " not found.");
            return;
        }
        Ticket ticket = found.get();
        ticket.displayDetails();

        boolean confirmed = input.getConfirmation(
            "  Are you sure you want to delete ticket " + code + "?");
        if (!confirmed) {
            System.out.println("  Deletion cancelled.");
            return;
        }

        repository.delete(ticket);
        System.out.println("   Ticket " + code + " deleted.\n");
    }

    // search
    public void searchTickets() {
        System.out.println("\n── Search Tickets ─────────────────────────────");
        System.out.println("  1. By Ticket ID");
        System.out.println("  2. By Customer Name");
        System.out.println("  3. By Category");
        System.out.println("  4. By Status");
        System.out.println("  5. By Priority");

        int choice = input.getMenuChoice("  Select option   : ", 1, 5);

        List<Ticket> results = switch (choice) {
            case 1 -> searchByID();
            case 2 -> searchByName();
            case 3 -> searchByCategory();
            case 4 -> searchByStatus();
            case 5 -> searchByPriority();
            default -> List.of();
        };

        displaySearchResults(results);
    }

    private List<Ticket> searchByID() {
        String code = input.getString("  Ticket ID : ").toUpperCase();
        return repository.findByCode(code).map(List::of).orElse(List.of());
    }

    private List<Ticket> searchByName() {
        String name = input.getString("  Customer Name : ");
        return repository.findByCustomerNameContaining(name);
    }

    private List<Ticket> searchByCategory() {
        printCategoryMenu();
        int choice = input.getMenuChoice("  Select category : ", 1, Ticket.CATEGORIES.length);
        String cat = Validator.categoryFromChoice(choice);
        return repository.findByCategory(cat);
    }

    private List<Ticket> searchByStatus() {
        printStatusMenu();
        int choice = input.getMenuChoice("  Select status : ", 1, Status.values().length);
        Status status = Validator.statusFromChoice(choice);
        return repository.findByStatus(status);
    }

    private List<Ticket> searchByPriority() {
        printPriorityMenu();
        int choice = input.getMenuChoice("  Select priority : ", 1, Priority.values().length);
        Priority priority = Validator.priorityFromChoice(choice);
        return repository.findByPriority(priority);
    }

    private void displaySearchResults(List<Ticket> results) {
        if (results.isEmpty()) {
            System.out.println("  No matching tickets found.\n");
            return;
        }
        System.out.printf("  %d result(s) found:%n", results.size());
        for (Ticket t : results) {
            t.displayDetails();
        }
    }

    // report
    public void displayReport() {
        reporter.displayReport(repository.findAll());
    }

    // print menus
    private void printCategoryMenu() {
        System.out.println("  Categories:");
        for (int i = 0; i < Ticket.CATEGORIES.length; i++) {
            System.out.printf("    %d. %s%n", i + 1, Ticket.CATEGORIES[i]);
        }
    }

    private void printPriorityMenu() {
        System.out.println("  Priorities:");
        Priority[] vals = Priority.values();
        for (int i = 0; i < vals.length; i++) {
            System.out.printf("    %d. %s%n", i + 1, vals[i].name());
        }
    }

    private void printStatusMenu() {
        System.out.println("  Statuses:");
        Status[] vals = Status.values();
        for (int i = 0; i < vals.length; i++) {
            System.out.printf("    %d. %s%n", i + 1, vals[i].name());
        }
    }
}
