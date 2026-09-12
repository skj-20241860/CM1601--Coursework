import java.awt.*;
import java.util.ArrayList;

public class InventoryManager {
    private final ArrayList<InventoryItem> items;

    public InventoryManager(ArrayList<InventoryItem> items) {
        this.items = items;
    }

    public ArrayList<InventoryItem> getItems() { return items; }

    public InventoryItem findItemByCode(String code) {
        if (code == null) return null;
        for (InventoryItem item : items) {
            if (item.getPartCode().equalsIgnoreCase(code.trim())) return item;
        }
        return null;
    }

    public boolean addItem(InventoryItem item) {
        if (item == null || findItemByCode(item.getPartCode()) != null) return false;
        items.add(item);
        AuditLogger.log("ADD_PART", item.getPartCode(), item.getQuantity());
        return true;
    }

    public boolean updateItem(String code, String name, String brand, double price,
                              int quantity, String category) {
        InventoryItem item = findItemByCode(code);
        if (item == null) return false;
        item.setPartName(name);
        item.setBrand(brand);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setCategory(category);
        AuditLogger.log("UPDATE_PART", code, quantity);
        return true;
    }

    public boolean deleteItem(String code) {
        InventoryItem item = findItemByCode(code);
        if (item == null) return false;
        boolean removed = items.remove(item);
        if (removed) AuditLogger.log("DELETE_PART", code, item.getQuantity());
        return removed;
    }

    public void sortByCategoryAndCode() {
        // Manual bubble sort: category first, then part code.
        for (int i = 0; i < items.size() - 1; i++) {
            for (int j = 0; j < items.size() - i - 1; j++) {
                InventoryItem first = items.get(j);
                InventoryItem second = items.get(j + 1);
                int categoryCompare = first.getCategory().compareToIgnoreCase(second.getCategory());
                boolean swap = categoryCompare > 0
                        || (categoryCompare == 0
                        && first.getPartCode().compareToIgnoreCase(second.getPartCode()) > 0);
                if (swap) {
                    items.set(j, second);
                    items.set(j + 1, first);
                }
            }
        }
    }

    public ArrayList<InventoryItem> search(String keyword, String category,
                                            double minimumPrice, double maximumPrice) {
        ArrayList<InventoryItem> results = new ArrayList<>();
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        String selectedCategory = category == null ? "" : category.trim();

        for (InventoryItem item : items) {
            boolean keywordMatch = key.isEmpty()
                    || item.getPartCode().toLowerCase().contains(key)
                    || item.getPartName().toLowerCase().contains(key)
                    || item.getBrand().toLowerCase().contains(key);
            boolean categoryMatch = selectedCategory.isEmpty()
                    || selectedCategory.equalsIgnoreCase("All")
                    || item.getCategory().equalsIgnoreCase(selectedCategory);
            boolean priceMatch = item.getPrice() >= minimumPrice && item.getPrice() <= maximumPrice;

            if (keywordMatch && categoryMatch && priceMatch) results.add(item);
        }
        return results;
    }

    public int getTotalParts() { return items.size(); }

    public double getTotalValue() {
        double total = 0;
        for (InventoryItem item : items) total += item.getPrice() * item.getQuantity();
        return total;
    }


    // Console-version compatibility methods.
    public void viewAllItems() {
        sortByCategoryAndCode();
        if (items.isEmpty()) {
            System.out.println("No inventory items found.");
            return;
        }
        System.out.printf("%-6s %-18s %-12s %-10s %-5s %-12s %-15s%n",
                "Code",
                "Name",
                "Brand",
                "Price",
                "Qty",
                "Category",
                "Image");
        System.out.println("----------------------------------------------------------------------------------------");
        for (InventoryItem item : items) {

            String status;

            if (item.getImagePath() == null ||
                    item.getImagePath().trim().isEmpty() ||
                    item.getImagePath().equalsIgnoreCase("no_image.png")) {

                status = "No Image";

            } else {

                status = "Image Found";
            }

            System.out.printf(
                    "%-6s %-18s %-12s Rs.%-8.2f %-5d %-12s %-15s%n",
                    item.getPartCode(),
                    item.getPartName(),
                    item.getBrand(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getCategory(),
                    item.getImagePath(),
                    status
            );
        }
        System.out.println("Total parts: " + getTotalParts());
        System.out.printf("Total inventory value: Rs. %.2f%n", getTotalValue());
    }

    public boolean updateItem(String code, double price, int quantity) {
        InventoryItem item = findItemByCode(code);
        if (item == null || price <= 0 || quantity < 0) return false;
        item.setPrice(price);
        item.setQuantity(quantity);
        AuditLogger.log("UPDATE_PART", code, quantity);
        return true;
    }

    public void searchItems(String category, double minimumPrice,
                            double maximumPrice, String keyword) {
        ArrayList<InventoryItem> results = search(keyword, category, minimumPrice, maximumPrice);
        if (results.isEmpty()) {
            System.out.println("No matching items found.");
            return;
        }
        for (InventoryItem item : results) {
            System.out.printf("%s | %s | %s | Rs. %.2f | Qty: %d | %s%n",
                    item.getPartCode(), item.getPartName(), item.getBrand(), item.getPrice(),
                    item.getQuantity(), item.getCategory());
        }
        System.out.println(results.size() + " matching item(s) found.");
    }

    public void showLowStockItems() {
        int threshold = FileHandler.loadThreshold("low_stock_threshold.txt", 5);
        boolean found = false;
        System.out.println("Low-stock threshold: " + threshold);
        for (InventoryItem item : items) {
            if (item.getQuantity() < threshold) {
                found = true;
                System.out.printf("%s | %s | Quantity: %d%n",
                        item.getPartCode(), item.getPartName(), item.getQuantity());
            }
        }
        if (!found) System.out.println("No low-stock items found.");
    }

}
