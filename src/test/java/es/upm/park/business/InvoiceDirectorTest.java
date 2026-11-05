package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class InvoiceDirectorTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final User luis = new User("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "6", "1111222233334444");
    private final InvoiceDirector director = new InvoiceDirector();

    private GroupTicket tg01() {
        Group g = new Group("G01", "Familia");
        g.addMember(ana);
        g.addMember(pedro);
        g.addMember(luis);
        return GroupTicket.purchase("TG01", DAY, g, P40);
    }

    /** Records the steps it receives, to check the director's sequence. */
    private static final class RecordingBuilder extends InvoiceBuilder {
        final List<String> steps = new ArrayList<>();

        @Override
        public void reset(String recipient, LocalDate date) {
            super.reset(recipient, date);
            steps.add("reset");
        }

        @Override
        public void buildIndividualTickets(List<IndividualTicket> tickets) {
            steps.add("individual");
        }

        @Override
        public void buildGroupTickets(List<GroupTicket> groupTickets) {
            steps.add("group");
        }

        @Override
        public void buildPasses(List<es.upm.park.business.model.Pass> passes) {
            steps.add("passes");
        }
    }

    @Test
    void directorAlwaysRunsTheSameSteps() {
        RecordingBuilder b = new RecordingBuilder();
        director.construct(b, "X", DAY, List.of(), List.of(), List.of());
        assertEquals(List.of("reset", "individual", "group", "passes"), b.steps);
    }

    @Test
    void userBuilderChargesOnlyDirectTickets() {
        IndividualTicket t001 = new IndividualTicket("T001", DAY, P40, ana);
        Invoice inv = director.construct(new UserInvoiceBuilder(), "USER ana@correo.com - Ana Garcia", DAY,
                List.of(t001), List.of(tg01()), List.of());
        assertEquals(1, inv.getSections().size());
        assertEquals("Individual tickets", inv.getSections().get(0).title());
        assertEquals(new BigDecimal("40.00"), inv.getTotal());
        assertTrue(!inv.format().contains("TG01"));
    }

    @Test
    void groupBuilderChargesGroupTicketAmountOnly() {
        IndividualTicket t005 = new IndividualTicket("T005", DAY, P40, ana);
        Invoice inv = director.construct(new GroupInvoiceBuilder(), "GROUP G01 - Familia", DAY,
                List.of(t005), List.of(tg01()), List.of());
        assertEquals(1, inv.getSections().size());
        assertEquals(new BigDecimal("108.00"), inv.getTotal());
        String text = inv.format();
        assertTrue(text.contains("TG01 2026-10-15 (3 tickets, 10% discount) 108.00"), text);
        assertTrue(!text.contains("T005") && !text.contains("TG01-"));
    }

    @Test
    void emptySectionsAreOmittedAndTotalIsZero() {
        Invoice inv = director.construct(new UserInvoiceBuilder(), "USER pedro@correo.com - Pedro Lopez", DAY,
                List.of(), List.of(), List.of());
        assertTrue(inv.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), inv.getTotal());
        assertEquals("INVOICE USER pedro@correo.com - Pedro Lopez" + System.lineSeparator()
                + "Date: 2026-10-15" + System.lineSeparator() + "Total: 0.00", inv.format());
    }

    @Test
    void builderCanBeReusedAndProductIsImmutable() {
        UserInvoiceBuilder b = new UserInvoiceBuilder();
        Invoice first = director.construct(b, "A", DAY, List.of(new IndividualTicket("T1", DAY, P40, ana)), List.of(), List.of());
        Invoice second = director.construct(b, "B", DAY, List.of(), List.of(), List.of());
        assertEquals(1, first.getSections().size());
        assertTrue(second.getSections().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> first.getSections().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.getSections().get(0).lines().clear());
    }
}
