package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
            "612345678", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    @Test
    public void datePredicates() {
        Ticket t = new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana);
        assertTrue(t.isActiveOn(DAY.minusDays(1)));
        assertTrue(t.isActiveOn(DAY));
        assertFalse(t.isActiveOn(DAY.plusDays(1)));
        assertTrue(t.isRefundableOn(DAY.minusDays(1)));
        assertFalse(t.isRefundableOn(DAY));
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.plusDays(1)));
    }

    @Test
    public void groupTicketUsersComeFromConstituents() {
        Group g = new Group("G01", "Familia");
        GroupTicket gt = new GroupTicket("TG01", DAY, new BigDecimal("80.00"), g, List.of(
                new IndividualTicket("TG01-1", DAY, new BigDecimal("40.00"), ana),
                new IndividualTicket("TG01-2", DAY, new BigDecimal("40.00"), pedro)));
        assertEquals(List.of(ana, pedro), gt.getUsers());
    }
}
