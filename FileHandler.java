import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String[] DATE_PATTERNS = {
            "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy", "d MMM yyyy", "MMM d, yyyy"
    };

    public static ArrayList<InventoryItem> loadInventory(String filename) {
        ArrayList<InventoryItem> items = new ArrayList<>();
        Path path = Path.of(filename);
        if (!Files.exists(path)) return items;

        try {
            List<String> lines = Files.readAllLines(path);
            for (String rawLine : lines) {
                InventoryItem item = parseInventoryLine(rawLine);
                if (item != null && !containsCode(items, item.getPartCode())) items.add(item);
            }
        } catch (IOException e) {
            System.err.println("Inventory load error: " + e.getMessage());
        }
        return items;
    }

    private static InventoryItem parseInventoryLine(String rawLine) {
        if (rawLine == null) return null;
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) return null;

        // Handles commas, semicolons, pipes and tabs, including extra spaces.
        String[] parts = line.split("\\s*[|;\\t,]+\\s*");
        if (parts.length < 5) return null;

        String code = clean(parts[0]);
        String name = parts.length > 1 ? clean(parts[1]) : "Unknown Part";
        String brand = parts.length > 2 && !clean(parts[2]).isEmpty() ? clean(parts[2]) : "Unknown";
        double price = parseMoney(parts.length > 3 ? parts[3] : "0");
        int quantity = parseInteger(parts.length > 4 ? parts[4] : "0");
        String category = parts.length > 5 ? InventoryItem.normaliseCategory(parts[5]) : "Other";
        String date = parts.length > 6 ? normaliseDate(parts[6]) : LocalDate.now().toString();
        String image = parts.length > 7 && !clean(parts[7]).isEmpty() ? clean(parts[7]) : "no_image.png";

        if (code.isEmpty() || name.isEmpty() || price < 0 || quantity < 0) return null;
        return new InventoryItem(code, name, brand, price, quantity, category, date, image);
    }

    public static ArrayList<Dealer> loadDealers(String filename) {
        ArrayList<Dealer> dealers = new ArrayList<>();
        Path path = Path.of(filename);
        if (!Files.exists(path)) return dealers;

        try {
            for (String rawLine : Files.readAllLines(path)) {
                String line = rawLine == null ? "" : rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s*[|;\\t,]+\\s*");
                if (parts.length < 2) continue;
                String id = clean(parts[0]);
                String name = clean(parts[1]);
                String phone = parts.length > 2 && !clean(parts[2]).isEmpty() ? clean(parts[2]) : "Not provided";
                String location = parts.length > 3 && !clean(parts[3]).isEmpty() ? clean(parts[3]) : "Unknown";
                if (!id.isEmpty() && !name.isEmpty() && !containsDealer(dealers, id)) {
                    dealers.add(new Dealer(id, name, phone, location));
                }
            }
        } catch (IOException e) {
            System.err.println("Dealer load error: " + e.getMessage());
        }
        return dealers;
    }

    public static void saveInventory(String filename, ArrayList<InventoryItem> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (InventoryItem item : items) {
                writer.printf("%s | %s | %s | Rs. %.2f | %d | %s | %s | %s%n",
                        item.getPartCode(), item.getPartName(), item.getBrand(), item.getPrice(),
                        item.getQuantity(), item.getCategory(), item.getDateAdded(), item.getImagePath());
            }
        } catch (IOException e) {
            System.err.println("Inventory save error: " + e.getMessage());
        }
    }

    public static int loadThreshold(String filename, int defaultValue) {
        Path path = Path.of(filename);
        if (!Files.exists(path)) {
            saveThreshold(filename, defaultValue);
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(Files.readString(path).trim());
            return value > 0 ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void saveThreshold(String filename, int value) {
        try { Files.writeString(Path.of(filename), String.valueOf(value)); }
        catch (IOException e) { System.err.println("Threshold save error: " + e.getMessage()); }
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("^[\\\"']|[\\\"']$", "").trim();
    }

    private static double parseMoney(String value) {
        String cleaned = clean(value).replaceAll("(?i)(rs\\.?|lkr|usd|\\$|£|€)", "")
                .replace(" ", "").replace(",", "");
        try { return Double.parseDouble(cleaned); }
        catch (NumberFormatException e) { return 0; }
    }

    private static int parseInteger(String value) {
        String cleaned = clean(value).replaceAll("[^0-9-]", "");
        try { return Integer.parseInt(cleaned); }
        catch (NumberFormatException e) { return 0; }
    }

    private static String normaliseDate(String value) {
        String text = clean(value);
        if (text.isEmpty()) return LocalDate.now().toString();
        for (String pattern : DATE_PATTERNS) {
            try {
                return LocalDate.parse(text, DateTimeFormatter.ofPattern(pattern)).toString();
            } catch (DateTimeParseException ignored) { }
        }
        return LocalDate.now().toString();
    }

    private static boolean containsCode(ArrayList<InventoryItem> items, String code) {
        for (InventoryItem item : items) if (item.getPartCode().equalsIgnoreCase(code)) return true;
        return false;
    }

    private static boolean containsDealer(ArrayList<Dealer> dealers, String id) {
        for (Dealer dealer : dealers) if (dealer.getDealerId().equalsIgnoreCase(id)) return true;
        return false;
    }
}
