package services;

import models.Ticket;
import models.Ticket.Priority;
import models.Ticket.Status;
import storage.FileStorage;
import utils.InputHelper;
import utils.TicketIDGenerator;
import utils.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class TicketService {

    private final LinkedHashMap<String, Ticket> tickets;
    private final HashMap<String, List<Ticket>> categoryIndex;
    private final FileStorage storage;
    private final InputHelper input;
    private final ReportService reporter;
    private final TicketIDGenerator idGen;

    public TicketService(FileStorage storage, InputHelper input) {
        this.storage  = storage;
        this.input    = input;
        this.reporter = new ReportService();

        FileStorage.LoadResult loaded = storage.loadFromFile();
        this.tickets = loaded.tickets;
        this.idGen   = new TicketIDGenerator(loaded.highestId);

        this.categoryIndex = new HashMap<>();
        for (String cat : Ticket.CATEGORIES) {
            categoryIndex.put(cat, new ArrayList<>());
        }
        for (Ticket t : tickets.values()) {
            categoryIndex.computeIfAbsent(t.getCategory(), k -> new ArrayList<>()).add(t);
        }
    }

    //ticket creation
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

        String id = idGen.next();
        Ticket ticket = new Ticket(id, name, phone, category, priority, description);

        tickets.put(id, ticket);
        categoryIndex.computeIfAbsent(category, k -> new ArrayList<>()).add(ticket);

        storage.saveToFile(tickets.values());

        System.out.println("\n   Ticket created successfully.");
        System.out.printf("    Ticket ID : %s | Status : OPEN | Date : %s%n%n",
            id, ticket.getCreationDate());
    }

    //retrieving all tickets
    public void viewAllTickets() {
        System.out.println("\n── All Tickets ────────────────────────────────");

        if (tickets.isEmpty()) {
            System.out.println("  No tickets found.");
            return;
        }

        String header = String.format("%-6s  %-20s  %-16s  %-8s  %-11s",
            "ID", "Customer", "Category", "Priority", "Status");
        String rule   = "─".repeat(header.length());

        System.out.println("  " + rule);
        System.out.println("  " + header);
        System.out.println("  " + rule);
        for (Ticket t : tickets.values()) {
            System.out.println("  " + t.toTableRow());
        }
        System.out.println("  " + rule);
        System.out.printf("  %d ticket(s) total.%n%n", tickets.size());
    }

    // retrieving ticket details
    public void viewTicketDetails() {
        System.out.println("\n── View Ticket Details ────────────────────────");
        String id = input.getString("  Enter Ticket ID : ").toUpperCase();

        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            System.out.println("  ! Ticket " + id + " not found.");
            return;
        }
        ticket.displayDetails();
    }

//updating a ticket
    public void updateTicket() {
        System.out.println("\n── Update Ticket ──────────────────────────────");
        String id = input.getString("  Enter Ticket ID : ").toUpperCase();

        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            System.out.println("  ! Ticket " + id + " not found.");
            return;
        }

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

        storage.saveToFile(tickets.values());
        System.out.printf("  Ticket %s updated successfully.%n%n", id);
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

    //delete
    public void deleteTicket() {
        System.out.println("\n── Delete Ticket ──────────────────────────────");
        String id = input.getString("  Enter Ticket ID : ").toUpperCase();

        Ticket ticket = tickets.get(id);
        if (ticket == null) {
            System.out.println("  ! Ticket " + id + " not found.");
            return;
        }
    
        ticket.displayDetails();

        boolean confirmed = input.getConfirmation(
            "  Are you sure you want to delete ticket " + id + "?");
        if (!confirmed) {
            System.out.println("  Deletion cancelled.");
            return;
        }

        tickets.remove(id);
        List<Ticket> catList = categoryIndex.get(ticket.getCategory());
        if (catList != null) catList.remove(ticket);

        storage.saveToFile(tickets.values());
        System.out.println("   Ticket " + id + " deleted.\n");
    }

    // Search
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
            default -> new ArrayList<>();
        };

        displaySearchResults(results);
    }

    private List<Ticket> searchByID() {
        String id = input.getString("  Ticket ID : ").toUpperCase();
        List<Ticket> result = new ArrayList<>();
        Ticket found = tickets.get(id);
        if (found != null) result.add(found);
        return result;
    }

    private List<Ticket> searchByName() {
        String name = input.getString("  Customer Name : ").toLowerCase();
        List<Ticket> results = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t.getCustomerName().toLowerCase().contains(name)) {
                results.add(t);
            }
        }
        return results;
    }

    private List<Ticket> searchByCategory() {
        printCategoryMenu();
        int choice = input.getMenuChoice("  Select category : ", 1, Ticket.CATEGORIES.length);
        String cat = Validator.categoryFromChoice(choice);
        List<Ticket> indexed = categoryIndex.getOrDefault(cat, new ArrayList<>());
        return new ArrayList<>(indexed);
    }

    private List<Ticket> searchByStatus() {
        printStatusMenu();
        int choice = input.getMenuChoice("  Select status : ", 1, Status.values().length);
        Status status = Validator.statusFromChoice(choice);
        List<Ticket> results = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t.getStatus() == status) results.add(t);
        }
        return results;
    }

    private List<Ticket> searchByPriority() {
        printPriorityMenu();
        int choice = input.getMenuChoice("  Select priority : ", 1, Priority.values().length);
        Priority priority = Validator.priorityFromChoice(choice);
        List<Ticket> results = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t.getPriority() == priority) results.add(t);
        }
        return results;
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

    //report
    public void displayReport() {
        reporter.displayReport(tickets.values());
    }

    // PRINT MENUS
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
