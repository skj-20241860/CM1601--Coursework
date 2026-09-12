import java.util.ArrayList;

public class CartManager {
    private final InventoryManager inventoryManager;
    private final ArrayList<CartItem> cart = new ArrayList<>();

    public CartManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public ArrayList<CartItem> getCart() { return cart; }
    public boolean isEmpty() { return cart.isEmpty(); }
    public void clearCart() { cart.clear(); }

    public String addToCart(String code, int quantity) {
        if (quantity <= 0) return "Quantity must be greater than zero.";
        InventoryItem item = inventoryManager.findItemByCode(code);
        if (item == null) return "Part code not found.";

        CartItem existing = null;
        for (CartItem cartItem : cart) {
            if (cartItem.getItem().getPartCode().equalsIgnoreCase(code)) {
                existing = cartItem;
                break;
            }
        }

        int alreadyInCart = existing == null ? 0 : existing.getQuantity();
        if (alreadyInCart + quantity > item.getQuantity()) {
            return "Not enough stock. Available: " + item.getQuantity()
                    + ", already in cart: " + alreadyInCart;
        }

        if (existing == null) cart.add(new CartItem(item, quantity));
        else existing.addQuantity(quantity);
        return null;
    }

    public double calculateBeforeSynergy() {
        double total = 0;
        for (CartItem cartItem : cart) total += cartItem.getDiscountedSubtotal();
        return total;
    }

    public boolean hasSynergyDiscount() {
        boolean engine = false;
        boolean electrical = false;
        for (CartItem cartItem : cart) {
            if (cartItem.getItem().getCategory().equalsIgnoreCase("Engine")) engine = true;
            if (cartItem.getItem().getCategory().equalsIgnoreCase("Electrical")) electrical = true;
        }
        return engine && electrical;
    }

    public double calculateTotal() {
        double total = calculateBeforeSynergy();
        return hasSynergyDiscount() ? total * 0.90 : total;
    }

    public String buildReceipt() {
        StringBuilder receipt = new StringBuilder("CART SUMMARY\n\n");
        for (CartItem cartItem : cart) {
            InventoryItem item = cartItem.getItem();
            receipt.append(item.getPartCode()).append(" - ").append(item.getPartName())
                    .append(" x ").append(cartItem.getQuantity())
                    .append(" = Rs. ").append(String.format("%.2f", cartItem.getDiscountedSubtotal()));
            if (cartItem.getQuantity() >= 3) receipt.append(" (5% bulk discount)");
            receipt.append("\n");
        }
        receipt.append("\nSubtotal: Rs. ").append(String.format("%.2f", calculateBeforeSynergy()));
        if (hasSynergyDiscount()) receipt.append("\nSynergy discount: 10%");
        receipt.append("\nFINAL TOTAL: Rs. ").append(String.format("%.2f", calculateTotal()));
        return receipt.toString();
    }

    public void checkout() {
        if (cart.isEmpty()) throw new IllegalStateException("Cannot process an empty cart.");
        for (CartItem cartItem : cart) {
            InventoryItem item = cartItem.getItem();
            if (cartItem.getQuantity() <= 0 || cartItem.getQuantity() > item.getQuantity()) {
                throw new IllegalStateException("Invalid quantity for " + item.getPartCode());
            }
        }
        for (CartItem cartItem : cart) {
            InventoryItem item = cartItem.getItem();
            item.setQuantity(item.getQuantity() - cartItem.getQuantity());
            AuditLogger.log("CHECKOUT", item.getPartCode(), cartItem.getQuantity());
        }
        cart.clear();
    }


    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        System.out.println(buildReceipt());
    }

}
