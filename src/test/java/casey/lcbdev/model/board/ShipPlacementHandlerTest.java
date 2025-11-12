package casey.lcbdev.model.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import casey.lcbdev.model.ships.Ship;

public class ShipPlacementHandlerTest {
    private Board<Ship> board;

    @BeforeEach
    void setup() {
        board = new Board<>(5,5,(x,y) -> new ShipCell(x,y));
    }

    @Test
    void testShipSupplier() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Supplier<Ship> supplier = () -> { latch.countDown(); return new Ship("Test",1,null); };

        ShipPlacementHandler handler = new ShipPlacementHandler(board, supplier, 1, ship -> {});

        Ship s = supplier.get();
        assertEquals("Test", s.getName());
        latch.await();
    }
}
