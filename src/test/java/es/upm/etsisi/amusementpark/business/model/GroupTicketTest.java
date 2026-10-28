package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static Group groupOf(int members) {
        Group group = new Group("G", "Grupo");
        for (int i = 0; i < members; i++) {
            group.addMember(new User("DNI" + i, "user" + i + "@correo.com"));
        }
        return group;
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,40.00", "3,36.00", "5,36.00", "6,32.00", "10,32.00", "11,20.00"})
    void discountBrackets(int members, String unitPrice) {
        GroupTicket ticket = GroupTicket.purchase("TG", LocalDate.of(2027, 4, 15), groupOf(members),
                new BigDecimal("40.00"));
        for (IndividualTicket part : ticket.getIndividualTickets()) {
            assertEquals(new BigDecimal(unitPrice), part.getUnitPrice());
        }
        assertEquals(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(members)), ticket.getPrice());
    }

    @Test
    void sixMembersAtFortyCost192() {
        GroupTicket ticket = GroupTicket.purchase("TG02", LocalDate.of(2027, 4, 15), groupOf(6),
                new BigDecimal("40.00"));
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
    }

    @Test
    void compositionHasDerivedIdentifiersAndIsFixed() {
        Group group = groupOf(2);
        GroupTicket ticket = GroupTicket.purchase("TG01", LocalDate.of(2027, 4, 15), group, new BigDecimal("40.00"));
        assertEquals("TG01:DNI0", ticket.getIndividualTickets().get(0).getIdentifier());
        assertSame(group.getMembers().get(1), ticket.getIndividualTickets().get(1).getUser());
        group.removeMember("user0@correo.com");
        assertEquals(2, ticket.getIndividualTickets().size());
        assertThrows(UnsupportedOperationException.class, () -> ticket.getIndividualTickets().clear());
        assertSame(group, ticket.getGroup());
        ticket.detachGroup();
        assertEquals(null, ticket.getGroup());
    }
}
