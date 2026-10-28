package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static List<User> members(int n) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            users.add(new User("U" + i, "S", String.format("%08dX", i), "u" + i + "@correo.com", "Clave1!", "6",
                    "1234567890123456"));
        }
        return users;
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,40.00", "3,36.00", "5,36.00", "6,32.00", "10,32.00", "11,20.00"})
    void discountBrackets(int n, String individual) {
        GroupTicket ticket = new GroupTicket("TG", DAY, new Group("G", "g"), members(n), FORTY);
        ticket.individualTickets().forEach(t -> assertEquals(new BigDecimal(individual), t.getPrice()));
        assertEquals(new BigDecimal(individual).multiply(BigDecimal.valueOf(n)), ticket.getPrice());
    }

    @Test
    void sixMembersAtFortyCost192() {
        GroupTicket ticket = new GroupTicket("TG01", DAY, new Group("G01", "g"), members(6), FORTY);
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
    }

    @Test
    void compositionHasDerivedIdsAndUsers() {
        List<User> users = members(2);
        Group group = new Group("G01", "g");
        GroupTicket ticket = new GroupTicket("TG01", DAY, group, users, FORTY);
        assertEquals("TG01:" + users.get(0).getDni(), ticket.individualTickets().get(0).getId());
        assertSame(users.get(1), ticket.individualTickets().get(1).getUser());
        assertTrue(ticket.individualTickets().stream().allMatch(IndividualTicket::isPartOfGroupTicket));
        assertTrue(ticket.individualTickets().stream().allMatch(t -> t.getDate().equals(DAY)));
        assertSame(group, ticket.getGroup());
    }

    @Test
    void compositionIsFixedAndGroupCanBeDetached() {
        Group group = new Group("G01", "g");
        List<User> users = members(2);
        users.forEach(u -> group.addMember(u.getDni()));
        GroupTicket ticket = new GroupTicket("TG01", DAY, group, users, FORTY);
        group.removeMember(users.get(0).getDni());
        assertEquals(2, ticket.individualTickets().size());
        ticket.detachGroup();
        assertNull(ticket.getGroup());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }
}
