import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

public class MainController {
    private static final String INVENTORY_FILE = "inventory_legacy.txt";
    private static final String DEALER_FILE = "dealers_legacy.txt";
    private static final String THRESHOLD_FILE = "low_stock_threshold.txt";

    @FXML private Label titleLabel;
    @FXML private Label statusLabel;
    @FXML private Label lowStockLabel;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> colCode;
    @FXML private TableColumn<InventoryItem, String> colName;
    @FXML private TableColumn<InventoryItem, String> colBrand;
    @FXML private TableColumn<InventoryItem, Double> colPrice;
    @FXML private TableColumn<InventoryItem, Integer> colQuantity;
    @FXML private TableColumn<InventoryItem, String> colCategory;
    @FXML private TableColumn<InventoryItem, String> colDateAdded;
    @FXML private TableColumn<InventoryItem, String> colImage;

    private InventoryManager inventoryManager;
    private CartManager cartManager;
    private int lowStockThreshold;

    @FXML
    public void initialize() {
        titleLabel.setText("Malabe Spares Depot Inventory System");
        colCode.setCellValueFactory(new PropertyValueFactory<>("partCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDateAdded.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        colImage.setCellValueFactory(cellData ->
                new SimpleStringProperty(getImageValue(cellData.getValue())));
        inventoryTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        inventoryManager = new InventoryManager(FileHandler.loadInventory(INVENTORY_FILE));
        cartManager = new CartManager(inventoryManager);
        lowStockThreshold = FileHandler.loadThreshold(THRESHOLD_FILE, 5);
        handleViewInventory();
    }

    @FXML private void handleViewInventory() {
        inventoryManager.sortByCategoryAndCode();
        showItems(inventoryManager.getItems());
    }

    @FXML private void handleAddPart() {
        String code = ask("Add Part", "Part code:");
        if (code == null) return;
        if (code.isBlank() || inventoryManager.findItemByCode(code) != null) {
            error("Invalid or duplicate part code."); return;
        }
        String name = ask("Add Part", "Part name:");
        String brand = ask("Add Part", "Brand:");
        Double price = askDouble("Add Part", "Price:", 0.01, Double.MAX_VALUE);
        Integer quantity = askInteger("Add Part", "Quantity:", 0, Integer.MAX_VALUE);
        String category = ask("Add Part", "Category (Engine/Electrical/Bodywork/Brakes):");
        if (name == null || name.isBlank() || brand == null || brand.isBlank()
                || price == null || quantity == null || category == null || category.isBlank()) return;

        String imageName = ask("Add Part", "Image name (optional):");
        if (imageName == null || imageName.isBlank()) {
            imageName = "no_image.png";
        }

        InventoryItem item = new InventoryItem(code.trim(), name.trim(), brand.trim(), price,
                quantity, category.trim(), LocalDate.now().toString(), imageName.trim());
        inventoryManager.addItem(item);
        saveAndRefresh("Part added successfully.");
    }

    @FXML private void handleUpdatePart() {
        InventoryItem item = selectedItem();
        if (item == null) return;
        String name = askWithDefault("Update Part", "Part name:", item.getPartName());
        String brand = askWithDefault("Update Part", "Brand:", item.getBrand());
        Double price = askDoubleWithDefault("Update Part", "Price:", item.getPrice());
        Integer quantity = askIntegerWithDefault("Update Part", "Quantity:", item.getQuantity());
        String category = askWithDefault("Update Part", "Category:", item.getCategory());
        if (name == null || brand == null || price == null || quantity == null || category == null) return;
        inventoryManager.updateItem(item.getPartCode(), name, brand, price, quantity, category);
        saveAndRefresh("Part updated successfully.");
    }

    @FXML private void handleDeletePart() {
        InventoryItem item = selectedItem();
        if (item == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + item.getPartCode() + " - " + item.getPartName() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            inventoryManager.deleteItem(item.getPartCode());
            saveAndRefresh("Part deleted successfully.");
        }
    }

    @FXML private void handleSearchParts() {
        String keyword = ask("Search", "Keyword (code/name/brand, blank allowed):");
        if (keyword == null) return;
        String category = ask("Search", "Category or All:");
        if (category == null) return;
        Double minimum = askDouble("Search", "Minimum price:", 0, Double.MAX_VALUE);
        Double maximum = askDouble("Search", "Maximum price:", 0, Double.MAX_VALUE);
        if (minimum == null || maximum == null) return;
        if (minimum > maximum) { error("Minimum price cannot exceed maximum price."); return; }
        ArrayList<InventoryItem> results = inventoryManager.search(keyword, category, minimum, maximum);
        showItems(results);
        statusLabel.setText(results.size() + " matching item(s). Three filters applied.");
    }

    @FXML private void handleLowStock() {
        ArrayList<InventoryItem> results = new ArrayList<>();
        for (InventoryItem item : inventoryManager.getItems()) {
            if (item.getQuantity() <= lowStockThreshold) results.add(item);
        }
        showItems(results);
    }

    @FXML private void handleSetThreshold() {
        Integer value = askInteger("Low Stock Threshold", "New threshold:", 1, Integer.MAX_VALUE);
        if (value == null) return;
        lowStockThreshold = value;
        FileHandler.saveThreshold(THRESHOLD_FILE, value);
        updateLowStockWarning();
        information("Threshold saved as " + value + ".");
    }

    @FXML private void handleDealerSelection() {
        ArrayList<Dealer> dealers = FileHandler.loadDealers(DEALER_FILE);
        if (dealers.size() < 4) { error("At least four unique dealers are required."); return; }
        ArrayList<Dealer> selected = new ArrayList<>();
        Random random = new Random();
        while (selected.size() < 4) {
            Dealer candidate = dealers.get(random.nextInt(dealers.size()));
            boolean duplicate = false;
            for (Dealer dealer : selected) {
                if (dealer.getDealerId().equalsIgnoreCase(candidate.getDealerId())) { duplicate = true; break; }
            }
            if (!duplicate) selected.add(candidate);
        }
        // Manual bubble sort by location.
        for (int i = 0; i < selected.size() - 1; i++) {
            for (int j = 0; j < selected.size() - i - 1; j++) {
                if (selected.get(j).getLocation().compareToIgnoreCase(selected.get(j + 1).getLocation()) > 0) {
                    Dealer temp = selected.get(j); selected.set(j, selected.get(j + 1)); selected.set(j + 1, temp);
                }
            }
        }
        StringBuilder text = new StringBuilder();
        for (Dealer dealer : selected) {
            text.append(dealer.getDealerId()).append(" | ").append(dealer.getDealerName()).append(" | ")
                    .append(dealer.getPhoneNumber()).append(" | ").append(dealer.getLocation()).append("\n");
        }
        TextArea area = new TextArea(text.toString()); area.setEditable(false); area.setPrefSize(600, 250);
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle("Selected Dealers");
        alert.setHeaderText("Four unique dealers sorted by location"); alert.getDialogPane().setContent(area); alert.showAndWait();
    }

    @FXML private void handlePointOfSale() {
        cartManager.clearCart();
        while (true) {
            String code = ask("Point of Sale", "Part code (Cancel to finish adding):");
            if (code == null) break;
            Integer quantity = askInteger("Point of Sale", "Quantity:", 1, Integer.MAX_VALUE);
            if (quantity == null) break;
            String problem = cartManager.addToCart(code, quantity);
            if (problem != null) error(problem); else information("Added to cart.");
        }
        if (cartManager.isEmpty()) { error("Cannot process an empty cart."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, cartManager.buildReceipt() + "\n\nProcess checkout?",
                ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            double total = cartManager.calculateTotal();
            try {
                cartManager.checkout();
                saveAndRefresh("Checkout complete. Total: Rs. " + String.format("%.2f", total));
            } catch (IllegalStateException e) { error(e.getMessage()); }
        } else cartManager.clearCart();
    }

    @FXML private void handleExit() { saveBeforeExit(); Platform.exit(); }
    public void saveBeforeExit() { FileHandler.saveInventory(INVENTORY_FILE, inventoryManager.getItems()); }

    private void saveAndRefresh(String message) {
        FileHandler.saveInventory(INVENTORY_FILE, inventoryManager.getItems());
        handleViewInventory();
        information(message);
    }


    private void showItems(ArrayList<InventoryItem> items) {
        inventoryTable.setItems(FXCollections.observableArrayList(items));
        inventoryTable.refresh();
        statusLabel.setText(items.size() + " displayed | Total inventory parts: "
                + inventoryManager.getTotalParts() + " | Total value: Rs. "
                + String.format("%.2f", inventoryManager.getTotalValue()));
        updateLowStockWarning();
    }

    private void updateLowStockWarning() {
        int count = 0;
        for (InventoryItem item : inventoryManager.getItems()) if (item.getQuantity() <= lowStockThreshold) count++;
        lowStockLabel.setText("Low-stock threshold: " + lowStockThreshold + " | Warning items: " + count);
    }

    private InventoryItem selectedItem() {
        InventoryItem item = inventoryTable.getSelectionModel().getSelectedItem();
        if (item == null) error("Select an inventory row first.");
        return item;
    }

    private String ask(String title, String label) { return askWithDefault(title, label, ""); }
    private String askWithDefault(String title, String label, String value) {
        TextInputDialog dialog = new TextInputDialog(value); dialog.setTitle(title); dialog.setHeaderText(null); dialog.setContentText(label);
        Optional<String> result = dialog.showAndWait(); return result.map(String::trim).orElse(null);
    }
    private Double askDouble(String title, String label, double min, double max) {
        while (true) {
            String text = ask(title, label); if (text == null) return null;
            try { double value = Double.parseDouble(text.replace(",", "")); if (value < min || value > max) throw new NumberFormatException(); return value; }
            catch (NumberFormatException e) { error("Enter a valid number between " + min + " and " + max + "."); }
        }
    }
    private Double askDoubleWithDefault(String title, String label, double current) {
        while (true) {
            String text = askWithDefault(title, label, String.valueOf(current)); if (text == null) return null;
            try { double value = Double.parseDouble(text); if (value <= 0) throw new NumberFormatException(); return value; }
            catch (NumberFormatException e) { error("Enter a price greater than zero."); }
        }
    }
    private Integer askInteger(String title, String label, int min, int max) {
        while (true) {
            String text = ask(title, label); if (text == null) return null;
            try { int value = Integer.parseInt(text); if (value < min || value > max) throw new NumberFormatException(); return value; }
            catch (NumberFormatException e) { error("Enter a whole number between " + min + " and " + max + "."); }
        }
    }
    private Integer askIntegerWithDefault(String title, String label, int current) {
        while (true) {
            String text = askWithDefault(title, label, String.valueOf(current)); if (text == null) return null;
            try { int value = Integer.parseInt(text); if (value < 0) throw new NumberFormatException(); return value; }
            catch (NumberFormatException e) { error("Enter zero or a positive whole number."); }
        }
    }

    /**
     * Reads the image value without depending on one exact getter name.
     * It supports getImage(), getImageName(), getImageFile(), or getImagePath().
     */
    private String getImageValue(InventoryItem item) {
        if (item == null) return "no_image.png";

        String[] getterNames = {"getImage", "getImageName", "getImageFile", "getImagePath"};

        for (String getterName : getterNames) {
            try {
                Object value = item.getClass().getMethod(getterName).invoke(item);
                if (value != null && !value.toString().trim().isEmpty()) {
                    return value.toString().trim();
                }
            } catch (ReflectiveOperationException ignored) {
                // Try the next possible getter name.
            }
        }

        return "no_image.png";
    }

    private void error(String message) { new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait(); }
    private void information(String message) { new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait(); }
}