package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParkGroupTicketTest extends ParkTestSupport {

    private Group familia() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        return park.findGroup("G01");
    }

    @Test
    void composesOneTicketPerMemberWithDerivedIds() {
        Group g = familia();
        GroupTicket tg = tickets.createGroupTicket("TG01", "G01", TODAY_S);
        assertSame(g, tg.getGroup());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"),
                tg.getComposition().stream().map(Ticket::getId).toList());
        assertEquals(g.getMembers(), tg.getHolders());
        assertSame(tg.getComposition().get(0), park.findTicket("TG01:12345678Z"));
    }

    @ParameterizedTest
    @CsvSource({"1, 0, 40.00", "2, 0, 80.00", "3, 10, 108.00", "5, 10, 180.00", "6, 20, 192.00",
        "10, 20, 320.00", "11, 50, 220.00"})
    void discountBrackets(int members, int discount, String total) {
        groupOf("GX", members);
        GroupTicket tg = tickets.createGroupTicket("TGX", "GX", TODAY_S);
        assertEquals(discount, tg.getComposition().get(0).getDiscountPercent());
        assertEquals(new BigDecimal(total), tg.getPrice());
        assertEquals(members, tg.getComposition().size());
    }

    @Test
    void sixMembersCostThirtyTwoEach() {
        groupOf("G02", 6);
        GroupTicket tg = tickets.createGroupTicket("TG02", "G02", TODAY_S);
        for (IndividualTicket t : tg.getComposition()) {
            assertEquals(new BigDecimal("32.00"), t.getPrice());
        }
    }

    @Test
    void discountUsesCurrentPrice() {
        familia();
        users.create("Luis", "Ruiz", dni(11111111), "luis@correo.com", "Clave1!", "600", "1111222233334444");
        groups.add("G01", "luis@correo.com");
        park.setBasePrice("50");
        assertEquals(new BigDecimal("135.00"), tickets.createGroupTicket("TG01", "G01", TODAY_S).getPrice());
    }

    @Test
    void emptyMissingGroupOrPastDateRejected() {
        groups.create("G00", "Vacio");
        assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG00", "G00", TODAY_S));
        assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG00", "G99", TODAY_S));
        familia();
        assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG01", "G01", YESTERDAY_S));
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void derivedIdCollisionCreatesNothing() {
        familia();
        tickets.create("TG01:87654321X", "pedro@correo.com", TODAY_S);
        assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG01", "G01", TODAY_S));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertEquals(1, park.getTickets().size());
        assertTrue(!park.isGroupLocked(park.findGroup("G01")));
    }

    @Test
    void compositionFixedAtPurchase() {
        familia();
        GroupTicket tg = tickets.createGroupTicket("TG01", "G01", TODAY_S);
        park.setToday(TOMORROW_S);
        groups.remove("G01", "pedro@correo.com");
        assertEquals(2, tg.getComposition().size());
        assertThrows(UnsupportedOperationException.class, () -> tg.getComposition().clear());
    }

    @Test
    void futureDeletionRemovesComposition() {
        familia();
        tickets.createGroupTicket("TG01", "G01", TOMORROW_S);
        tickets.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertNull(park.findTicket("TG01:87654321X"));
    }

    @Test
    void todaysGroupTicketKept() {
        familia();
        tickets.createGroupTicket("TG01", "G01", TODAY_S);
        assertThrows(ParkException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> tickets.deleteGroupTicket("NOPE"));
        assertNotNull(park.findTicket("TG01"));
    }

    @Test
    void userDeletionRespectsGroupTickets() {
        familia();
        tickets.createGroupTicket("TG01", "G01", TODAY_S);
        UserException active = assertThrows(UserException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        assertEquals("User has active tickets", active.getMessage());
        assertEquals(2, park.findGroup("G01").getMembers().size());
    }

    @Test
    void memberOfLockedGroupWithoutOwnTicketCannotBeDeleted() throws java.io.IOException {
        // Only reachable from stored data: Pedro is a member of G01, but G01's active ticket covers only Ana.
        java.nio.file.Files.write(dir.resolve("users.txt"), List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;" + HASHER.hash("Clave1!") + ";612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;" + HASHER.hash("Otra2#") + ";623456789;9876543210987654"));
        java.nio.file.Files.write(dir.resolve("groups.txt"), List.of("G01;Familia;12345678Z;87654321X"));
        java.nio.file.Files.write(dir.resolve("tickets.txt"), List.of(
                "G;TG01;" + TODAY_S + ";G01",
                "C;TG01:12345678Z;" + TODAY_S + ";40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez"));
        restart();
        User pedro = park.findUser("pedro@correo.com");
        assertTrue(!park.hasActiveTicket(pedro));
        UserException e = assertThrows(UserException.class, () -> users.delete("pedro@correo.com", "Otra2#"));
        assertEquals("User belongs to locked group G01", e.getMessage());
        assertTrue(park.findGroup("G01").contains(pedro));
    }

    @Test
    void deletionRemovesUserFromModifiableGroups() {
        familia();
        groups.create("G02", "Amigos");
        groups.add("G02", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY_S);
        park.setToday(TOMORROW_S);
        users.delete("pedro@correo.com", "Otra2#");
        assertNull(park.findUser("pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
        assertTrue(park.findGroup("G02").getMembers().isEmpty());
        GroupTicket tg = (GroupTicket) park.findTicket("TG01");
        assertEquals(2, tg.getHolders().size(), "historical composition preserved");
    }
}
