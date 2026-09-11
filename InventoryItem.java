public class InventoryItem {
    private String partCode;
    private String partName;
    private String brand;
    private double price;
    private int quantity;
    private String category;
    private String dateAdded;
    private String imagePath;

    public InventoryItem(String partCode, String partName, String brand, double price,
                         int quantity, String category, String dateAdded, String imagePath) {
        this.partCode = partCode;
        this.partName = partName;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.category = normaliseCategory(category);
        this.dateAdded = dateAdded;
        this.imagePath = imagePath;
    }

    public static String normaliseCategory(String value) {
        if (value == null || value.isBlank()) return "Other";
        String text = value.trim().toLowerCase();
        if (text.startsWith("eng")) return "Engine";
        if (text.startsWith("elec")) return "Electrical";
        if (text.startsWith("body")) return "Bodywork";
        if (text.startsWith("brak")) return "Brakes";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public String getPartCode() { return partCode; }
    public String getPartName() { return partName; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public String getDateAdded() { return dateAdded; }
    public String getImagePath() { return imagePath; }

    public void setPartName(String partName) { this.partName = partName; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCategory(String category) { this.category = normaliseCategory(category); }
}
