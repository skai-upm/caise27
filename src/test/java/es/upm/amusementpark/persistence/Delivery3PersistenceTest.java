package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.UserDirectory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Delivery3PersistenceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;
    private ParkStorage storage;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(new UserDirectory() {
            @Override
            public boolean exists(String email) {
                return true;
            }

            @Override
            public String dniOf(String email) {
                return "DNI-" + email;
            }
        });
        storage = ParkStorage.inDirectory(dir);
        park.setRepository(storage);
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.createIndividualTicket("T002", "ana@correo.com", TODAY.plusDays(3));
    }

    private void restart() {
        park.saveState();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
    }

    @Test
    void storedPricesAndStatesRebuilt() throws IOException {
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpecialPass("PE01", "T001");
        park.markPassConsumed("SP01:1");
        park.saveState();
        assertTrue(Files.readString(dir.resolve("passes.txt")).contains("SPEED;SP01:1;T001;8.00;true"));
        restart();
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:1").getPrice());
        assertTrue(park.findPass("SP01:1").isConsumed());
        assertEquals(new BigDecimal("40.00"), park.findPass("PE01").getPrice());
        assertFalse(park.findPass("PE01").isConsumed());
    }

    @Test
    void storedPriceIsUsedAndLegacyLinesDerivePrice() throws IOException {
        Files.writeString(dir.resolve("tickets.txt"), "INDIVIDUAL;T001;2027-04-15;40.00;ana@correo.com\n");
        Files.writeString(dir.resolve("passes.txt"), "SPEED;SP09:1;T001;7.50;false\nSPECIAL;PE09;T001;false\n");
        park.reset();
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
        assertEquals(new BigDecimal("7.50"), park.findPass("SP09:1").getPrice());
        assertEquals(new BigDecimal("40.00"), park.findPass("PE09").getPrice());
    }

    @Test
    void cascadedDeletionsArePersisted() {
        park.buySpeedPasses("SP02", "T002", 1);
        park.deleteTicket("T002");
        restart();
        assertNull(park.findTicket("T002"));
        assertNull(park.findPass("SP02:1"));
        assertEquals(1, park.getTickets().size());
    }
}
