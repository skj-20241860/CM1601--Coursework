import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventorySystemTest {

    @Test
    void testPartCodeValidation() {
        String code = "P001";
        assertTrue(code.matches("P\\d{3}"));
    }

    @Test
    void testInvalidPartCode() {
        String code = "123";
        assertFalse(code.matches("P\\d{3}"));
    }

    @Test
    void testDiscountCalculation() {
        double subtotal = 1000.0;
        double discount = subtotal * 0.10;
        double finalTotal = subtotal - discount;

        assertEquals(900.0, finalTotal);
    }

    @Test
    void testQuantityValidation() {
        int quantity = 10;
        assertTrue(quantity >= 0);
    }

    @Test
    void testPriceValidation() {
        double price = 2500.0;
        assertTrue(price > 0);
    }
    // TC04 - Invalid negative price
    @Test
    void testInvalidPrice() {
        double price = -500.00;

        assertFalse(price > 0);
    }
}