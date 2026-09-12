import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {
    private static final String LOG_FILE = "audit_log.txt";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String action, String itemCode, Integer quantity) {
        String quantityText = quantity == null ? "N/A" : String.valueOf(quantity);
        String line = LocalDateTime.now().format(FORMAT)
                + " | ACTION=" + action
                + " | ITEM=" + itemCode
                + " | QUANTITY=" + quantityText;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Could not write audit log: " + e.getMessage());
        }
    }
}
