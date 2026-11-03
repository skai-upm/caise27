package es.upm.etsisi.amusementpark.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.Invoice;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceBuilderTest {

    static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    static final LocalDate ISSUE = LocalDate.of(2026, 10, 1);

    @Test
    void userBuilderOnlyAddsIndividualTickets() {
        User ana = TestData.user("12345678Z", "ana@correo.com");
        UserInvoiceBuilder b = new UserInvoiceBuilder(ana, List.of(new Ticket("T001", ana, DAY, new BigDecimal("40.00"))));
        b.buildHeader(ISSUE);
        b.buildGroupTickets();
        assertTrue(b.getResult().getSections().isEmpty());
        b.buildIndividualTickets();
        Invoice invoice = b.getResult();
        assertEquals(1, invoice.getSections().size());
        assertEquals("Individual tickets", invoice.getSections().get(0).getTitle());
        assertEquals("T001 2026-10-15", invoice.getSections().get(0).getLines().get(0).getDescription());
        assertEquals("USER ana@correo.com (N S)", invoice.getRecipient());
    }

    @Test
    void groupBuilderOnlyAddsGroupTickets() {
        Group g = TestData.groupOf(3);
        GroupInvoiceBuilder b = new GroupInvoiceBuilder(g, List.of(new GroupTicket("TG01", g, DAY, new BigDecimal("40.00"))));
        Invoice invoice = InvoiceDirector.construct(b, ISSUE);
        assertEquals(1, invoice.getSections().size());
        assertEquals("Group tickets", invoice.getSections().get(0).getTitle());
        assertEquals("TG01 2026-10-15 3 members, 10% discount", invoice.getSections().get(0).getLines().get(0).getDescription());
        assertEquals("108.00", invoice.getTotal().toPlainString());
        assertEquals("GROUP G01 (Familia)", invoice.getRecipient());
    }

    @Test
    void emptySectionsAreOmitted() {
        Invoice user = InvoiceDirector.construct(new UserInvoiceBuilder(TestData.user("12345678Z", "a@correo.com"), List.of()), ISSUE);
        Invoice group = InvoiceDirector.construct(new GroupInvoiceBuilder(TestData.groupOf(2), List.of()), ISSUE);
        assertTrue(user.getSections().isEmpty());
        assertTrue(group.getSections().isEmpty());
        assertEquals("0.00", user.getTotal().toPlainString());
    }

    @Test
    void directorRunsAllStepsAndHeaderRestarts() {
        User ana = TestData.user("12345678Z", "ana@correo.com");
        UserInvoiceBuilder b = new UserInvoiceBuilder(ana, List.of(new Ticket("T001", ana, DAY, new BigDecimal("40.00"))));
        assertThrows(IllegalStateException.class, b::getResult);
        assertEquals("40.00", InvoiceDirector.construct(b, ISSUE).getTotal().toPlainString());
        assertEquals("40.00", InvoiceDirector.construct(b, ISSUE).getTotal().toPlainString());
    }
}
