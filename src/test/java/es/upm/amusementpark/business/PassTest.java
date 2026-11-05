package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.PassCommands;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H");
    private ParkController park;

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
                return USERS.containsKey(email);
            }

            @Override
            public String dniOf(String email) {
                return USERS.get(email);
            }
        });
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.createIndividualTicket("T002", "ana@correo.com", TODAY.plusDays(5));
    }

    @Test
    void pricesDeriveFromTicketPrice() {
        assertEquals(new BigDecimal("8.00"), park.buySpeedPasses("SP01", "T001", 1).get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), park.buySpecialPass("PE01", "T001").getPrice());
        park.setCurrentPrice(new BigDecimal("99"));
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:1").getPrice());
    }

    @Test
    void discountedComponentPrice() {
        park.createGroup("G01", "F");
        for (String email : List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com")) {
            park.addUserToGroup("G01", email);
        }
        park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(new BigDecimal("7.20"), park.buySpeedPasses("SP05", "TG01:12345678Z", 1).get(0).getPrice());
    }

    @Test
    void packageOfTenWithDerivedIds() {
        List<SpeedPass> created = park.buySpeedPasses("SP02", "T001", 10);
        assertEquals(10, created.size());
        assertEquals("SP02:1", created.get(0).getId());
        assertEquals("SP02:10", created.get(9).getId());
        assertEquals(10, park.passesOf((IndividualTicket) park.findTicket("T001")).size());
    }

    @Test
    void invalidPurchasesCreateNothing() {
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T001", 5));
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T999", 1));
        park.setCurrentDate(TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T001", 1));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void groupTicketIsNotAnIndividualTicket() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "TG01", 1));
    }

    @Test
    void duplicatePassIdentifierRejected() {
        park.buySpecialPass("PE01", "T001");
        assertThrows(ParkException.class, () -> park.buySpecialPass("PE01", "T002"));
        assertThrows(ParkException.class, () -> park.restorePass("PE01", "T002", true, false));
    }

    @Test
    void conflictInPackageIsAtomic() {
        park.buySpecialPass("SP03:4", "T001");
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP03", "T002", 10));
        assertNull(park.findPass("SP03:1"));
        assertEquals(1, park.getPasses().size());
    }

    @Test
    void onlyOneSpecialPassEvenIfConsumed() {
        park.buySpecialPass("PE01", "T001");
        park.markPassConsumed("PE01");
        assertThrows(ParkException.class, () -> park.buySpecialPass("PE02", "T001"));
        assertNull(park.findPass("PE02"));
        assertTrue(park.findPass("PE01").isConsumed());
    }

    @Test
    void refundRules() {
        park.buySpeedPasses("SP02", "T002", 10);
        park.refundPass("SP02:3", SpeedPass.class);
        assertNull(park.findPass("SP02:3"));
        assertEquals(9, park.getPasses().size());
        park.markPassConsumed("SP02:4");
        assertThrows(ParkException.class, () -> park.refundPass("SP02:4", SpeedPass.class));
        park.buySpeedPasses("SP01", "T001", 1);
        assertThrows(ParkException.class, () -> park.refundPass("SP01:1", SpeedPass.class));
        assertThrows(ParkException.class, () -> park.refundPass("SP02:5", SpecialPass.class));
        assertThrows(ParkException.class, () -> park.refundPass("NOPE", SpeedPass.class));
        assertNotNull(park.findPass("SP02:5"));
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        park.buySpeedPasses("SP01", "T002", 1);
        park.buySpeedPasses("SP02", "T002", 1);
        park.buySpecialPass("PE01", "T002");
        park.buySpeedPasses("SP09", "T001", 1);
        park.deleteTicket("T002");
        assertNull(park.findPass("SP01:1"));
        assertNull(park.findPass("SP02:1"));
        assertNull(park.findPass("PE01"));
        assertNotNull(park.findPass("SP09:1"));
    }

    @Test
    void rejectedTicketDeletionKeepsPasses() {
        park.buySpeedPasses("SP09", "T001", 1);
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertNotNull(park.findPass("SP09:1"));
    }

    @Test
    void groupTicketDeletionCascadesToComponentPasses() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(3));
        park.buySpeedPasses("SP05", "TG01:12345678Z", 1);
        park.deleteGroupTicket("TG01");
        assertNull(park.findPass("SP05:1"));
        assertNull(park.findTicket("TG01:12345678Z"));
    }

    @Test
    void userInvoiceIncludesPersonalPasses() {
        park.createGroup("G01", "F");
        for (String email : List.of("ana@correo.com", "pedro@correo.com", "luis@correo.com")) {
            park.addUserToGroup("G01", email);
        }
        park.createGroupTicket("TG01", "G01", TODAY);
        park.deleteTicket("T002");
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpecialPass("PE02", "TG01:12345678Z");
        Invoice invoice = park.createUserInvoice("ana@correo.com");
        assertEquals(new BigDecimal("84.00"), invoice.total());
        assertEquals(2, invoice.sections().size());
        assertEquals(new BigDecimal("108.00"), park.createGroupInvoice("G01").total());
    }

    @Test
    void passesPersistWithConsumedFlag() {
        ParkStorage storage = ParkStorage.inDirectory(dir);
        park.setRepository(storage);
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpecialPass("PE01", "T001");
        park.markPassConsumed("PE01");
        park.saveState();
        park.reset();
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:1").getPrice());
        assertTrue(park.findPass("PE01").isConsumed());
        assertEquals("T001", park.findPass("PE01").getTicket().getId());
        assertEquals(park.findTicket("T001"), park.findPass("SP01:1").getTicket());
    }

    @Test
    void commandsShowPricesAndErrors() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        PassCommands.register(processor, park, out);
        processor.execute("SPEEDPASS CREATE;SP02;T002;10");
        processor.execute("SPEEDPASS CREATE;SP01;T001;1");
        processor.execute("SPEEDPASS CREATE;SP03;T001;5");
        processor.execute("SPECIALPASS CREATE;PE01;T001");
        processor.execute("SPECIALPASS CREATE;PE02;T001");
        processor.execute("SPEEDPASS DELETE;SP02:3");
        processor.execute("SPECIALPASS DELETE;PE01");
        processor.execute("SPECIALPASS DELETE;SP02:4");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("SPEEDPASS PURCHASED: 10 pass(es). TOTAL PRICE: 80.00"));
        assertTrue(text.contains("SPEEDPASS PURCHASED: 1 pass(es). TOTAL PRICE: 8.00"));
        assertTrue(text.contains("ERROR: Quantity must be 1 or 10"));
        assertTrue(text.contains("SPECIALPASS PURCHASED. PRICE: 40.00"));
        assertTrue(text.contains("ERROR: Ticket T001 already has Special Pass PE01"));
        assertTrue(text.contains("SPEEDPASS REFUNDED"));
        assertTrue(text.contains("ERROR: Pass PE01 belongs to a ticket for today or a past date"));
        assertTrue(text.contains("ERROR: Special Pass SP02:4 does not exist"));
    }
}
