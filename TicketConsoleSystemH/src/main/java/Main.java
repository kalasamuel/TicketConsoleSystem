import repository.HibernateTicketRepository;
import repository.TicketRepository;
import services.TicketService;
import storage.HibernateUtil;
import utils.InputHelper;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        InputHelper input = new InputHelper(scanner);
        TicketRepository repository = new HibernateTicketRepository();
        TicketService service = new TicketService(repository, input);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = input.getMenuChoice("Select Option: ", 1, 8);

            try {
                switch (choice) {
                    case 1 -> service.createTicket();
                    case 2 -> service.viewAllTickets();
                    case 3 -> service.viewTicketDetails();
                    case 4 -> service.updateTicket();
                    case 5 -> service.deleteTicket();
                    case 6 -> service.searchTickets();
                    case 7 -> service.displayReport();
                    case 8 -> {
                        System.out.println("\nGoodbye.\n");
                        running = false;
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("\n  ! Something went wrong: " + e.getMessage());
                System.out.println("  You're back at the main menu; your other tickets are unaffected.\n");
            }
        }

        scanner.close();
        HibernateUtil.shutdown();
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("══════════════════════════════════════");
        System.out.println("      CALL CENTER TICKETING SYSTEM    ");
        System.out.println("══════════════════════════════════════");
        System.out.println("  1. Create Ticket");
        System.out.println("  2. View All Tickets");
        System.out.println("  3. View Ticket Details");
        System.out.println("  4. Update Ticket");
        System.out.println("  5. Delete Ticket");
        System.out.println("  6. Search Tickets");
        System.out.println("  7. Reports");
        System.out.println("  8. Exit");
        System.out.println("══════════════════════════════════════");
    }
}
