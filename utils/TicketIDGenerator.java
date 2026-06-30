package utils;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketIDGenerator {

    private static final String PREFIX  = "T";
    private static final int    PADDING = 6;

    private final AtomicInteger counter;

    public TicketIDGenerator(int seed){this.counter = new AtomicInteger(seed);}

    public String next(){
        int n = counter.incrementAndGet();
        return PREFIX + String.format("%0" + PADDING + "d", n);
    }

    public static int extractNumber(String ticketID) {
        if (ticketID == null || !ticketID.startsWith(PREFIX)) return 0;
        try {
            return Integer.parseInt(ticketID.substring(PREFIX.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}