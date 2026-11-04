package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final BigDecimal FORTY = new BigDecimal("40.00");
    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00", "11,50,220.00"})
    void discountBrackets(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountPercent(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalFor(FORTY, members));
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        assertEquals(new BigDecimal("30.02"), GroupTicket.totalFor(new BigDecimal("11.12"), 3));
    }

    @Test
    void generatesOnePartPerCurrentMemberAsSnapshot() {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
        User luis = new User("Luis", "M", "11111111H", "luis@correo.com", "Luis3$", "3", "1111222233334444");
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        g.addMember(luis);

        GroupTicket t = new GroupTicket("TG01", DAY, g, FORTY);

        assertEquals(new BigDecimal("108.00"), t.getPrice());
        assertEquals(3, t.getParts().size());
        IndividualTicket first = t.getParts().get(0);
        assertEquals("TG01-12345678Z", first.getId());
        assertEquals(DAY, first.getDate());
        assertEquals(new BigDecimal("36.00"), first.getPrice());
        assertEquals(new BigDecimal("36.00"), GroupTicket.memberPrice(FORTY, 3));
        assertEquals(new BigDecimal("40.00"), GroupTicket.memberPrice(FORTY, 2));
        assertEquals(new BigDecimal("20.00"), GroupTicket.memberPrice(FORTY, 11));
        assertSame(ana, first.getUser().orElseThrow());
        assertTrue(t.belongsTo(g));

        g.removeMember(ana);
        assertEquals(3, t.getParts().size());
    }

    @Test
    void visitorsAreAllConstituentUsers() {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        Ticket t = new GroupTicket("TG01", DAY, g, FORTY);
        assertEquals(java.util.List.of(ana, pedro), t.getVisitors());
        ((GroupTicket) t).getParts().get(0).detachUser();
        assertEquals(java.util.List.of(pedro), t.getVisitors());
    }

    @Test
    void detachGroupKeepsId() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456"));
        GroupTicket t = new GroupTicket("TG01", DAY, g, FORTY);
        t.detachGroup();
        assertTrue(t.getGroup().isEmpty());
        assertFalse(t.belongsTo(g));
        assertEquals("G01", t.getGroupId());
    }

    @Test
    void ticketAbstractionIsShared() {
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456"));
        Ticket t = new GroupTicket("TG01", DAY, g, FORTY);
        assertTrue(t.isOnOrAfter(DAY));
        assertFalse(t.isOnOrAfter(DAY.plusDays(1)));
        assertEquals(new BigDecimal("40.00"), t.getPrice());
    }
}
