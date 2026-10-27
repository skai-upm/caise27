package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TicketTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");
    private final LocalDate day = LocalDate.of(2027, 4, 15);

    @Test
    void activeAndDeletableDependOnCurrentDate() {
        Ticket t = new IndividualTicket("T001", day, new BigDecimal("40.00"), ana);
        assertTrue(t.isActive(day.minusDays(1)));
        assertTrue(t.isActive(day));
        assertFalse(t.isActive(day.plusDays(1)));
        assertTrue(t.isDeletable(day.minusDays(1)));
        assertFalse(t.isDeletable(day));
        assertFalse(t.isDeletable(day.plusDays(1)));
    }

    @Test
    void individualTicketKnowsItsUserAndPrice() {
        IndividualTicket t = new IndividualTicket("T001", day, new BigDecimal("45.50"), ana);
        assertSame(ana, t.getUser());
        assertTrue(t.involves(ana));
        assertFalse(t.involves(pedro));
        assertEquals(new BigDecimal("45.50"), t.getPrice());
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
    }

    @Test
    void usersDoNotReferenceTickets() {
        for (Field f : User.class.getDeclaredFields()) {
            assertFalse(f.getType().getName().contains("Ticket") || f.getGenericType().getTypeName().contains("Ticket"),
                    f.getName());
        }
    }
}
