import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        ArrayList<InventoryItem> items =
                FileHandler.loadInventory("inventory_legacy.txt");

        ArrayList<Dealer> dealers =
                FileHandler.loadDealers("dealers_legacy.txt");

        InventoryManager inventoryManager = new InventoryManager(items);
        DealerManager dealerManager = new DealerManager(dealers);
        CartManager cartManager = new CartManager(inventoryManager);

        Scanner input = new Scanner(System.in);
        int choice = 0;

        while (choice != 11) {
            System.out.println("\n========== MALABE SPARES DEPOT ==========");
            System.out.println("1. View Inventory");
            System.out.println("2. Add Part");
            System.out.println("3. Update Part");
            System.out.println("4. Delete Part");
            System.out.println("5. Search Parts");
            System.out.println("6. View Low Stock Items");
            System.out.println("7. View All Dealers");
            System.out.println("8. Select Random Four Dealers");
            System.out.println("9. Add Item to Cart");
            System.out.println("10. Checkout Cart");
            System.out.println("11. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(input.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
                continue;
            }

            if (choice == 1) {
                inventoryManager.viewAllItems();

            } else if (choice == 2) {
                System.out.print("Enter part code: ");
                String code = input.nextLine().trim();

                if (code.isEmpty() || inventoryManager.findItemByCode(code) != null) {
                    System.out.println("Invalid or duplicate part code.");
                    continue;
                }

                System.out.print("Enter part name: ");
                String name = input.nextLine().trim();

                System.out.print("Enter brand: ");
                String brand = input.nextLine().trim();

                double price = readPositiveDouble(input, "Enter price: ");
                int quantity = readNonNegativeInteger(input, "Enter quantity: ");

                System.out.print("Enter category: ");
                String category = input.nextLine().trim();
                String date = LocalDate.now().toString();

                System.out.print("Enter image file or press Enter for no image: ");
                String image = input.nextLine().trim();



                InventoryItem item = new InventoryItem(
                        code, name, brand, price, quantity, category, date, image
                );

                if (inventoryManager.addItem(item)) {
                    FileHandler.saveInventory("inventory_legacy.txt", inventoryManager.getItems());
                    System.out.println("Part added successfully.");
                } else {
                    System.out.println("Part could not be added.");
                }

            } else if (choice == 3) {
                System.out.print("Enter part code to update: ");
                String code = input.nextLine().trim();

                double price = readPositiveDouble(input, "Enter new price: ");
                int quantity = readNonNegativeInteger(input, "Enter new quantity: ");

                if (inventoryManager.updateItem(code, price, quantity)) {
                    FileHandler.saveInventory("inventory_legacy.txt", inventoryManager.getItems());
                    System.out.println("Part updated successfully.");
                } else {
                    System.out.println("Part code not found.");
                }

            } else if (choice == 4) {
                System.out.print("Enter part code to delete: ");
                String code = input.nextLine().trim();

                if (inventoryManager.deleteItem(code)) {
                    FileHandler.saveInventory("inventory_legacy.txt", inventoryManager.getItems());
                    System.out.println("Part deleted successfully.");
                } else {
                    System.out.println("Part code not found.");
                }

            } else if (choice == 5) {
                System.out.print("Enter category or all: ");
                String category = input.nextLine().trim();

                double minPrice = readNonNegativeDouble(input, "Enter minimum price: ");
                double maxPrice = readNonNegativeDouble(input, "Enter maximum price: ");

                if (maxPrice < minPrice) {
                    System.out.println("Maximum price cannot be lower than minimum price.");
                    continue;
                }

                System.out.print("Enter keyword: ");
                String keyword = input.nextLine().trim();

                inventoryManager.searchItems(category, minPrice, maxPrice, keyword);

            } else if (choice == 6) {
                inventoryManager.showLowStockItems();

            } else if (choice == 7) {
                dealerManager.viewAllDealers();

            } else if (choice == 8) {
                dealerManager.selectRandomFourDealers();

            } else if (choice == 9) {
                System.out.print("Enter part code: ");
                String code = input.nextLine().trim();

                int quantity = readPositiveInteger(input, "Enter quantity: ");
                String error = cartManager.addToCart(code, quantity);

                if (error == null) {
                    System.out.println("Item added to cart.");
                    cartManager.viewCart();
                } else {
                    System.out.println(error);
                }

            } else if (choice == 10) {
                if (cartManager.isEmpty()) {
                    System.out.println("Cannot process an empty cart.");
                    continue;
                }

                cartManager.viewCart();
                double total = cartManager.calculateTotal();
                cartManager.checkout();
                FileHandler.saveInventory("inventory_legacy.txt", inventoryManager.getItems());
                System.out.printf("Checkout completed. Total: Rs. %.2f%n", total);

            } else if (choice == 11) {
                FileHandler.saveInventory("inventory_legacy.txt", inventoryManager.getItems());
                System.out.println("Exiting program...");

            } else {
                System.out.println("Invalid choice.");
            }
        }

        input.close();
    }

    private static double readPositiveDouble(Scanner input, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(input.nextLine());
                if (value > 0) return value;
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter numbers only.");
            }
        }
    }

    private static double readNonNegativeDouble(Scanner input, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(input.nextLine());
                if (value >= 0) return value;
                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter numbers only.");
            }
        }
    }

    private static int readPositiveInteger(Scanner input, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(input.nextLine());
                if (value > 0) return value;
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }

    private static int readNonNegativeInteger(Scanner input, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(input.nextLine());
                if (value >= 0) return value;
                System.out.println("Value cannot be negative.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }
}
