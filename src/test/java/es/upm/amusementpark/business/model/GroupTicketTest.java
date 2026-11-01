package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final BigDecimal FORTY = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1, 40.00", "2, 80.00", "3, 108.00", "5, 180.00", "6, 192.00", "10, 320.00", "11, 220.00",
            "20, 400.00"})
    void appliesDiscountBrackets(int members, String total) {
        assertEquals(new BigDecimal(total), GroupTicket.totalPrice(FORTY, members));
    }

    @Test
    void bracketBoundaries() {
        assertEquals(0, GroupTicket.discountFor(2).signum());
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(3));
        assertEquals(new BigDecimal("0.10"), GroupTicket.discountFor(5));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(6));
        assertEquals(new BigDecimal("0.20"), GroupTicket.discountFor(10));
        assertEquals(new BigDecimal("0.50"), GroupTicket.discountFor(11));
    }

    @Test
    void composesOneTicketPerMemberAtPurchase() {
        User ana = new User("Ana", "A", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        User pedro = new User("Pedro", "P", "87654321X", "pedro@correo.com", "Otra2#", "2", "1234567890123456");
        User luis = new User("Luis", "L", "11111111H", "luis@correo.com", "Otra2#", "3", "1234567890123456");
        Group group = new Group("G01", "Familia");
        group.addMember(ana);
        group.addMember(pedro);
        group.addMember(luis);

        GroupTicket ticket = new GroupTicket("TG01", group, LocalDate.of(2026, 10, 15), FORTY);
        group.removeMember(luis);

        assertSame(group, ticket.getGroup());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertEquals(List.of(ana, pedro, luis), ticket.getUsers());
        assertEquals(List.of("TG01-1", "TG01-2", "TG01-3"),
                ticket.getTickets().stream().map(Ticket::getId).toList());
        for (IndividualTicket constituent : ticket.getTickets()) {
            assertEquals(new BigDecimal("36.00"), constituent.getPrice());
            assertEquals(ticket.getDate(), constituent.getDate());
        }
    }

    @Test
    void ticketsShareAbstraction() {
        User ana = new User("Ana", "A", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        Ticket individual = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), FORTY, ana);
        assertEquals(List.of(ana), individual.getUsers());
        assertEquals(true, individual.isActiveOn(LocalDate.of(2026, 10, 15)));
        assertEquals(false, individual.isRefundableOn(LocalDate.of(2026, 10, 15)));
        assertEquals(true, individual.isRefundableOn(LocalDate.of(2026, 10, 14)));
        assertEquals(false, individual.isActiveOn(LocalDate.of(2026, 10, 16)));
    }
}
