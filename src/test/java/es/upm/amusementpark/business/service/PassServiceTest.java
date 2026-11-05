package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassServiceTest {

    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = ApplicationInitializer.initialize(Repositories.inMemory());
        for (String c : List.of("SYSTEM DATE;2027-04-01",
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "SYSTEM PRICE;50",
                "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com")) {
            assertFalse(p.process(c).startsWith("ERROR"), c);
        }
    }

    private Pass pass(String id) {
        return Park.getInstance().passes().findById(id).orElseThrow();
    }

    private boolean exists(String id) {
        return Park.getInstance().passes().findById(id).isPresent();
    }

    @Test
    void singleAndPackPurchasesPricedFromStoredTicketPrice() {
        assertEquals("SPEEDPASS CREATED: SP01:1. Total price: 8.00 EUR", p.process("SPEEDPASS CREATE;SP01;T001;1"));
        assertEquals("SPEEDPASS CREATED: SP02:1..SP02:10. Total price: 80.00 EUR", p.process("SPEEDPASS CREATE;SP02;T001;10"));
        for (int i = 1; i <= 10; i++) {
            assertEquals(new BigDecimal("8.00"), pass("SP02:" + i).getPrice());
            assertFalse(pass("SP02:" + i).isConsumed());
        }
        assertEquals("SPECIALPASS CREATED. Price: 40.00 EUR", p.process("SPECIALPASS CREATE;PE01;T001"));
    }

    @Test
    void discountedCompositionTicket() {
        p.process("SYSTEM PRICE;40");
        for (int i = 0; i < 4; i++) {
            String dni = String.format("%08d", 100 + i) + "TRWAGMYFPDXBNJZSQVHLCKE".charAt((100 + i) % 23);
            p.process("USER CREATE;U;S;" + dni + ";u" + i + "@correo.com;Clave1!;6;1234567890123456");
            p.process("GROUP ADD;G01;u" + i + "@correo.com");
        }
        p.process("GROUPTICKET CREATE;TG02;G01;2027-04-15");
        assertEquals("SPEEDPASS CREATED: SP03:1. Total price: 6.40 EUR", p.process("SPEEDPASS CREATE;SP03;TG02:12345678Z;1"));
    }

    @Test
    void invalidPurchasesCreateNothing() {
        p.process("SPEEDPASS CREATE;SP02;T001;1");
        assertTrue(p.process("SPEEDPASS CREATE;SP02;T001;10").startsWith("ERROR"));
        assertFalse(exists("SP02:2"));
        assertTrue(p.process("SPEEDPASS CREATE;SP05;T001;5").startsWith("ERROR"));
        assertTrue(p.process("SPEEDPASS CREATE;SP05;T999;1").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-04-16");
        assertTrue(p.process("SPEEDPASS CREATE;SP05;T001;1").startsWith("ERROR"));
        assertTrue(p.process("SPECIALPASS CREATE;PE05;T001").startsWith("ERROR"));
        assertFalse(exists("SP05:1"));
        assertEquals(1, Park.getInstance().passes().findAll().size());
    }

    @Test
    void secondSpecialPassRejectedEvenIfConsumed() {
        p.process("SPECIALPASS CREATE;PE01;T001");
        assertTrue(p.process("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        pass("PE01").consume();
        assertTrue(p.process("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        assertFalse(exists("PE02"));
    }

    @Test
    void refunds() {
        p.process("SPEEDPASS CREATE;SP02;T001;10");
        p.process("SPECIALPASS CREATE;PE01;T001");
        assertEquals("SPEEDPASS DELETED", p.process("SPEEDPASS DELETE;SP02:3"));
        assertFalse(exists("SP02:3"));
        assertTrue(exists("SP02:4"));
        assertTrue(p.process("SPECIALPASS DELETE;SP02:4").startsWith("ERROR"));
        pass("SP02:5").consume();
        assertTrue(p.process("SPEEDPASS DELETE;SP02:5").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-04-15");
        assertTrue(p.process("SPECIALPASS DELETE;PE01").startsWith("ERROR"));
        assertTrue(exists("PE01"));
        assertTrue(p.process("SPEEDPASS DELETE;NOPE").startsWith("ERROR"));
    }

    @Test
    void ticketDeletionCascades() {
        p.process("SPEEDPASS CREATE;SP01;T001;1");
        p.process("SPEEDPASS CREATE;SP02;T001;10");
        p.process("SPECIALPASS CREATE;PE01;T001");
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-20");
        p.process("SPEEDPASS CREATE;SP03;TG01:12345678Z;1");
        assertEquals("TICKET DELETED", p.process("TICKET DELETE;T001"));
        assertFalse(exists("SP01:1") || exists("SP02:10") || exists("PE01"));
        assertEquals("GROUP TICKET DELETED", p.process("GROUPTICKET DELETE;TG01"));
        assertTrue(Park.getInstance().passes().findAll().isEmpty());
    }

    @Test
    void rejectedTicketDeletionKeepsPasses() {
        p.process("SYSTEM DATE;2027-04-15");
        p.process("SPEEDPASS CREATE;SP01;T001;1");
        assertTrue(p.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(exists("SP01:1"));
    }

    @Test
    void invoicesIncludeUserPassesOnly() {
        p.process("SYSTEM PRICE;40");
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-20");
        p.process("SPEEDPASS CREATE;SP01;TG01:12345678Z;1");
        String user = p.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(user.contains("[SpeedPasses]"));
        assertTrue(user.contains("SP01:1 (ticket TG01:12345678Z) : 8.00 EUR"));
        assertFalse(user.contains("TG01:12345678Z (2027"));
        assertTrue(user.endsWith("TOTAL: 48.00 EUR"), user);
        assertTrue(p.process("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 80.00 EUR"));
    }
}
