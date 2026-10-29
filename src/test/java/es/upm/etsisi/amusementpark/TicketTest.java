package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketTest {

    static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    static final BigDecimal FORTY = new BigDecimal("40.00");

    static List<User> users(int n) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            users.add(new User("U" + i, "S", ParkFixture.validDni(20000000 + i), "u" + i + "@correo.com", "Clave1!",
                    "6", "1111222233334444"));
        }
        return users;
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "30,50"})
    void discountBrackets(int members, int percent) {
        assertEquals(percent, GroupDiscount.percentFor(members));
    }

    @Test
    void sixMembersAtFortyCost192() {
        GroupTicket ticket = new GroupTicket("TG02", DAY, FORTY, null, users(6));
        for (IndividualTicket part : ticket.getComposition()) {
            assertEquals(new BigDecimal("32.00"), part.getPrice());
        }
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
    }

    @Test
    void compositionIdsAndUsers() {
        List<User> members = users(2);
        Group group = new Group("G01", "Familia");
        GroupTicket ticket = new GroupTicket("TG01", DAY, FORTY, group, members);
        assertEquals("TG01:" + members.get(0).getDni(), ticket.getComposition().get(0).getId());
        assertEquals(members, ticket.getUsers());
        assertSame(group, ticket.getGroup());
        assertSame(ticket.getComposition().get(1), ticket.findTicket("TG01:" + members.get(1).getDni()));
        assertSame(ticket, ticket.findTicket("TG01"));
        assertNull(ticket.findTicket("TG02"));
    }

    @Test
    void compositionCannotBeModified() {
        GroupTicket ticket = new GroupTicket("TG01", DAY, FORTY, null, users(2));
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> ticket.getComposition().clear());
    }

    @Test
    void activeAndValidity() {
        Ticket ticket = new IndividualTicket("T1", DAY, FORTY, 0, users(1).get(0));
        assertTrue(ticket.isActive(DAY));
        assertTrue(ticket.isActive(DAY.minusDays(1)));
        assertFalse(ticket.isActive(DAY.plusDays(1)));
        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.plusDays(1)));
        assertEquals(FORTY, ticket.getPrice());
    }

    @Test
    void ticketTypesShareCommonAbstraction() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }

    @Test
    void usersAndGroupsHoldNoTicketReferences() {
        for (Class<?> type : List.of(User.class, Group.class)) {
            for (java.lang.reflect.Field f : type.getDeclaredFields()) {
                assertFalse(f.getGenericType().getTypeName().contains("Ticket"), type + "." + f.getName());
                assertFalse(f.getGenericType().getTypeName().contains("Attraction"), type + "." + f.getName());
            }
        }
    }
}
