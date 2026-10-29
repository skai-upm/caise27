package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,40.00", "3,36.00", "5,36.00", "6,32.00", "10,32.00", "11,20.00"})
    void discountBrackets(int members, String expected) {
        assertEquals(new BigDecimal(expected), GroupTicket.discountedPrice(FORTY, members));
    }

    @Test
    void roundingHalfUp() {
        assertEquals(new BigDecimal("41.00"), GroupTicket.discountedPrice(new BigDecimal("45.55"), 3));
    }

    private static GroupTicket ticketFor(int members, Group group) {
        List<IndividualTicket> composition = new ArrayList<>();
        BigDecimal price = GroupTicket.discountedPrice(FORTY, members);
        for (int i = 0; i < members; i++) {
            User user = new User("u" + i + "@correo.com", "DNI" + i);
            composition.add(new IndividualTicket(GroupTicket.componentIdentifier("TG02", user.getDni()),
                    user, DAY, price));
        }
        return new GroupTicket("TG02", group, DAY, composition);
    }

    @Test
    void sixMembersTotal192() {
        GroupTicket ticket = ticketFor(6, new Group("G02", "Seis"));
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        for (IndividualTicket composed : ticket.getComposition()) {
            assertEquals(new BigDecimal("32.00"), composed.getPrice());
        }
    }

    @Test
    void identifiersIncludeComposition() {
        Collection<String> ids = ticketFor(2, null).identifiers();
        assertEquals(List.of("TG02", "TG02:DNI0", "TG02:DNI1"), List.copyOf(ids));
    }

    @Test
    void involvesComposedUsers() {
        GroupTicket ticket = ticketFor(2, null);
        assertTrue(ticket.involves(new User("u1@correo.com", "DNI1")));
        assertFalse(ticket.involves(new User("x@correo.com", "X")));
    }

    @Test
    void compositionIsUnmodifiable() {
        GroupTicket ticket = ticketFor(2, null);
        assertThrows(UnsupportedOperationException.class, () -> ticket.getComposition().clear());
    }

    @Test
    void groupReferenceReleasable() {
        Group group = new Group("G01", "G");
        GroupTicket ticket = ticketFor(1, group);
        assertSame(group, ticket.getGroup());
        ticket.releaseGroup();
        assertNull(ticket.getGroup());
    }

    @Test
    void noParallelUserCollectionAndGroupHoldsNoTickets() {
        for (Field field : GroupTicket.class.getDeclaredFields()) {
            if (Collection.class.isAssignableFrom(field.getType())) {
                assertEquals("composition", field.getName());
            }
        }
        for (Field field : Group.class.getDeclaredFields()) {
            assertFalse(Ticket.class.isAssignableFrom(field.getType()));
        }
    }
}
