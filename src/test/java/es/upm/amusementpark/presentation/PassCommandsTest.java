package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.controller.ApplicationInitializer;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

class PassCommandsTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444");
        processor.process("SYSTEM DATE;2027-04-10");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;pedro@correo.com;2027-04-10");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;luis@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private PassRepository passes() throws Exception {
        Field f = PassController.class.getDeclaredField("passes");
        f.setAccessible(true);
        return (PassRepository) f.get(PassController.getInstance());
    }

    @Test
    void buysSpeedPasses() throws Exception {
        assertEquals("SPEEDPASS CREATED - Total price: 8.00 EUR", processor.process("SPEEDPASS CREATE;SP01;T001;1"));
        assertEquals("SPEEDPASS CREATED - Total price: 80.00 EUR", processor.process("SPEEDPASS CREATE;SP02;T001;10"));
        assertNotNull(passes().findById("SP01:1"));
        assertNotNull(passes().findById("SP02:10"));
        assertFalse(passes().findById("SP02:10").isConsumed());
    }

    @Test
    void discountedComponentAndPriceChanges() {
        processor.process("SYSTEM PRICE;50");
        assertEquals("SPEEDPASS CREATED - Total price: 7.20 EUR", processor.process("SPEEDPASS CREATE;SP01;TG01:12345678Z;1"));
    }

    @Test
    void rejectsInvalidSpeedPassPurchases() throws Exception {
        assertEquals("ERROR: Quantity must be 1 or 10", processor.process("SPEEDPASS CREATE;SP01;T001;5"));
        assertEquals("ERROR: Individual ticket not found", processor.process("SPEEDPASS CREATE;SP01;T999;1"));
        assertEquals("ERROR: Individual ticket not found", processor.process("SPEEDPASS CREATE;SP01;TG01;1"));
        processor.process("SYSTEM DATE;2027-04-11");
        assertTrue(processor.process("SPEEDPASS CREATE;SP01;T002;1").startsWith("ERROR"));
        assertEquals(0, passes().findAll().size());
    }

    @Test
    void packageIsAtomic() throws Exception {
        processor.process("SPECIALPASS CREATE;SP03:7;T001");
        assertTrue(processor.process("SPEEDPASS CREATE;SP03;T001;10").startsWith("ERROR: Pass identifier already exists"));
        assertNull(passes().findById("SP03:1"));
        assertEquals(1, passes().findAll().size());
    }

    @Test
    void specialPassAtMostOnce() {
        assertEquals("SPECIALPASS CREATED - Price: 40.00 EUR", processor.process("SPECIALPASS CREATE;PE01;T001"));
        assertEquals("ERROR: Ticket already has a Special Pass", processor.process("SPECIALPASS CREATE;PE02;T001"));
        assertEquals("ERROR: Pass identifier already exists", processor.process("SPECIALPASS CREATE;PE01;TG01:87654321X"));
    }

    @Test
    void refunds() throws Exception {
        processor.process("SPEEDPASS CREATE;SP02;T001;10");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        processor.process("SPECIALPASS CREATE;PE02;T002");
        assertEquals("SPEEDPASS DELETED", processor.process("SPEEDPASS DELETE;SP02:3"));
        assertNull(passes().findById("SP02:3"));
        assertNotNull(passes().findById("SP02:1"));
        assertEquals("ERROR: SpeedPass not found", processor.process("SPEEDPASS DELETE;PE01"));
        assertTrue(processor.process("SPECIALPASS DELETE;PE02").startsWith("ERROR"));
        passes().findById("PE01").consume();
        assertEquals("ERROR: Consumed passes cannot be refunded", processor.process("SPECIALPASS DELETE;PE01"));
        assertEquals("ERROR: Ticket already has a Special Pass", processor.process("SPECIALPASS CREATE;PE03;T001"));
    }

    @Test
    void ticketDeletionRemovesItsPasses() throws Exception {
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("SPEEDPASS CREATE;SP02;T001;10");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        assertEquals("TICKET DELETED", processor.process("TICKET DELETE;T001"));
        assertEquals(0, passes().findAll().size());
    }

    @Test
    void groupTicketDeletionRemovesComponentPasses() throws Exception {
        processor.process("SPECIALPASS CREATE;PE01;TG01:12345678Z");
        processor.process("SPEEDPASS CREATE;SP01;TG01:87654321X;1");
        assertEquals("GROUPTICKET DELETED", processor.process("GROUPTICKET DELETE;TG01"));
        assertEquals(0, passes().findAll().size());
    }

    @Test
    void userInvoiceIncludesPassesGroupInvoiceDoesNot() {
        processor.process("SPEEDPASS CREATE;SP01;TG01:11111111H;1");
        String luis = processor.process("INVOICE CREATE;USER;luis@correo.com");
        assertEquals(String.join("\n",
                "INVOICE (USER)",
                "Name: Luis Ruiz",
                "DNI: 11111111H",
                "Email: luis@correo.com",
                "[SpeedPasses]",
                "  SP01:1 (ticket TG01:11111111H): 7.20 EUR",
                "TOTAL: 7.20 EUR"), luis);
        processor.process("SPECIALPASS CREATE;PE01;T001");
        assertTrue(processor.process("INVOICE CREATE;USER;ana@correo.com")
                .contains("[Special passes]\n  PE01 (ticket T001): 40.00 EUR\nTOTAL: 80.00 EUR"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 108.00 EUR"));
    }

    @Test
    void helpListsPassCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>"));
        assertTrue(help.contains("SPECIALPASS DELETE;<specialPassIdentifier>"));
    }
}
