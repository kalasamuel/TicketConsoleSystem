package storage;

import models.Ticket;
import utils.TicketIDGenerator;

import java.io.*;
import java.nio.file.*;
import java.util.Collection;
import java.util.LinkedHashMap;

public class FileStorage {

    private static final String FILE_PATH = "data/tickets.txt";

    public static class LoadResult {
        public final LinkedHashMap<String, Ticket> tickets;
        public final int highestId;

        LoadResult(LinkedHashMap<String, Ticket> tickets, int highestId) {
            this.tickets   = tickets;
            this.highestId = highestId;
        }
    }

//loading from file into linkedhashmap
    public LoadResult loadFromFile() {
        LinkedHashMap<String, Ticket> map = new LinkedHashMap<>();
        int highestId = 0;
        ensureFileExists();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Ticket t = Ticket.fromFileLine(line);
                    map.put(t.getTicketID(), t);
                    
                    int n = TicketIDGenerator.extractNumber(t.getTicketID());
                    if (n > highestId) highestId = n;

                } catch (Exception e) {
                    System.err.printf(
                        "[FileStorage] Warning: skipped corrupted line %d — %s%n",
                        lineNumber, e.getMessage()
                    );
                }
            }
        } catch (IOException e) {
            System.err.println("[FileStorage] Could not read tickets file: " + e.getMessage());
        }

        return new LoadResult(map, highestId);
    }

// savinf to file
    public void saveToFile(Collection<Ticket> tickets) {
        ensureFileExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Ticket t : tickets) {
                writer.write(t.toFileLine());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[FileStorage] Could not write tickets file: " + e.getMessage());
        }
    }

    private void ensureFileExists() {
        try {
            Path filePath = Paths.get(FILE_PATH);
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.err.println("[FileStorage] Could not initialise storage file: " + e.getMessage());
        }
    }
}
