import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartManagerTest {

    @Test
    void testDiscount() {
        double subtotal = 1000.0;
        double discount = subtotal * 0.10;
        double finalTotal = subtotal - discount;

        assertEquals(900.0, finalTotal);
    }

}
