public class CartItem {
    private final InventoryItem item;
    private int quantity;

    public CartItem(InventoryItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public InventoryItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void addQuantity(int amount) { quantity += amount; }

    public double getOriginalSubtotal() { return item.getPrice() * quantity; }
    public double getDiscountedSubtotal() {
        double subtotal = getOriginalSubtotal();
        return quantity >= 3 ? subtotal * 0.95 : subtotal;
    }
}
