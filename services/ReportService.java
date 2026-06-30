package services;

import models.Ticket;

import java.util.Collection;

public class ReportService {

    public void displayReport(Collection<Ticket> tickets){
        int total  =tickets.size();
        int open =0, inProgress=0, resolved=0, closed=0;
        int low =0, medium=0, high=0;
        int billing = 0, network=0, technical=0, complaint=0, general=0;

        for (Ticket t : tickets){
            switch (t.getStatus()) {
                case OPEN        -> open++;
                case IN_PROGRESS -> inProgress++;
                case RESOLVED    -> resolved++;
                case CLOSED      -> closed++;
            }

            switch (t.getPriority()) {
                case LOW    -> low++;
                case MEDIUM -> medium++;
                case HIGH   -> high++;
            }

            switch (t.getCategory()) {
                case "Billing"         -> billing++;
                case "Network"         -> network++;
                case "Technical"       -> technical++;
                case "Complaint"       -> complaint++;
                case "General Inquiry" -> general++;
            }
        }

        String sep = "═".repeat(40);
        System.out.println();
        System.out.println(sep);
        System.out.println("           TICKET REPORT");
        System.out.println(sep);
        System.out.printf("  %-22s : %d%n", "Total Tickets", total);
        System.out.println("  " + "─".repeat(36));
        System.out.printf("  %-22s : %d%n", "Open", open);
        System.out.printf("  %-22s : %d%n", "In Progress", inProgress);
        System.out.printf("  %-22s : %d%n", "Resolved", resolved);
        System.out.printf("  %-22s : %d%n", "Closed",closed);
        System.out.println("  " + "─".repeat(36));
        System.out.printf("  %-22s : %d%n", "High Priority", high);
        System.out.printf("  %-22s : %d%n", "Medium Priority", medium);
        System.out.printf("  %-22s : %d%n", "Low Priority", low);
        System.out.println("  " + "─".repeat(36));
        System.out.println("  By Category:");
        System.out.printf("    %-20s : %d%n", "Billing", billing);
        System.out.printf("    %-20s : %d%n", "Network", network);
        System.out.printf("    %-20s : %d%n", "Technical", technical);
        System.out.printf("    %-20s : %d%n", "Complaint", complaint);
        System.out.printf("    %-20s : %d%n", "General Inquiry", general);
        System.out.println(sep);
        System.out.println();
    }
}
