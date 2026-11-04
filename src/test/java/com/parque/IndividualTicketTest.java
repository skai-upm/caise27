package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class IndividualTicketTest {
    final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");

    @Test
    void holdsAllFieldsAndOwnerReference() {
        IndividualTicket t = new IndividualTicket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40"));
        assertEquals("T001", t.getId());
        assertSame(ana, t.getUser());
        assertEquals("12345678Z", t.getHolderDni());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertEquals("40.00", t.getPriceText());
    }

    @Test
    void historyTicketWithoutRegisteredUser() {
        IndividualTicket t = new IndividualTicket("T001", null, "12345678Z", LocalDate.of(2026, 10, 15), new BigDecimal("60.5"));
        assertNull(t.getUser());
        assertEquals("12345678Z", t.getHolderDni());
        assertEquals("60.50", t.getPriceText());
    }
}
