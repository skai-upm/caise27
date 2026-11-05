package es.upm.amusementpark.business;

import es.upm.amusementpark.presentation.help.HelpCommands;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.InvoiceCommands;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvoiceGenerationTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H", "eva@correo.com", "22222222J");
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(new UserDirectory() {
            @Override
            public boolean exists(String email) {
                return USERS.containsKey(email);
            }

            @Override
            public String dniOf(String email) {
                return USERS.get(email);
            }
        });
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.setCurrentPrice(new BigDecimal("45.50"));
        park.createIndividualTicket("T002", "ana@correo.com", TODAY.plusDays(1));
        park.setCurrentPrice(new BigDecimal("40.00"));
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.addUserToGroup("G01", "luis@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createGroupTicket("TG02", "G01", TODAY.plusDays(3));
    }

    private static List<String> texts(Invoice invoice, String section) {
        return invoice.sections().stream().filter(s -> s.title().equals(section)).findFirst().orElseThrow()
                .lines().stream().filter(Invoice.Line::isItem).map(Invoice.Line::text).toList();
    }

    @Test
    void userInvoiceExcludesGroupComponents() {
        Invoice invoice = park.createUserInvoice("ana@correo.com");
        assertEquals("12345678Z", invoice.recipient().get("DNI"));
        assertEquals(List.of("T001 (2027-04-15)", "T002 (2027-04-16)"), texts(invoice, UserInvoiceBuilder.TICKETS));
        assertEquals(new BigDecimal("85.50"), invoice.total());
    }

    @Test
    void groupInvoiceCountsGroupTicketsOnce() {
        Invoice invoice = park.createGroupInvoice("G01");
        assertEquals("Familia", invoice.recipient().get("Name"));
        assertEquals(2, texts(invoice, GroupInvoiceBuilder.GROUP_TICKETS).size());
        assertEquals(new BigDecimal("216.00"), invoice.total());
        long details = invoice.sections().get(0).lines().stream().filter(l -> !l.isItem()).count();
        assertEquals(6, details);
    }

    @Test
    void userWithoutPurchasesHasNoSections() {
        Invoice invoice = park.createUserInvoice("eva@correo.com");
        assertTrue(invoice.sections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.total());
    }

    @Test
    void unknownRecipientsRejected() {
        assertThrows(ParkException.class, () -> park.createUserInvoice("nobody@correo.com"));
        assertThrows(ParkException.class, () -> park.createGroupInvoice("G99"));
    }

    @Test
    void invoiceReflectsCurrentState() {
        assertEquals(new BigDecimal("85.50"), park.createUserInvoice("ana@correo.com").total());
        park.createIndividualTicket("T003", "ana@correo.com", TODAY);
        assertEquals(new BigDecimal("125.50"), park.createUserInvoice("ana@correo.com").total());
    }

    @Test
    void commandOutput() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        HelpCommands.registerDefault(processor, out);
        InvoiceCommands.register(processor, park, out);
        processor.execute("INVOICE CREATE;GROUP;G01");
        processor.execute("INVOICE CREATE;USER;eva@correo.com");
        processor.execute("INVOICE CREATE;COMPANY;X");
        processor.execute("INVOICE CREATE;USER");
        processor.execute("INVOICE CREATE;GROUP;G99");
        processor.execute("HELP");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("INVOICE - GROUP"));
        assertTrue(text.contains("  Name: Familia"));
        assertTrue(text.contains("GROUP TICKETS"));
        assertTrue(text.contains("  TG01 (2027-04-15, 3 members)  108.00"));
        assertTrue(text.contains("    - TG01:12345678Z ana@correo.com 36.00"));
        assertTrue(text.contains("TOTAL: 216.00"));
        assertTrue(text.contains("INVOICE - USER\n  Email: eva@correo.com\n  DNI: 22222222J\nTOTAL: 0.00"
                .replace("\n", System.lineSeparator())));
        assertTrue(text.contains("ERROR: Invalid invoice type"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        assertTrue(text.contains("ERROR: Group G99 does not exist"));
        assertTrue(text.contains("INVOICE CREATE;GROUP;<groupIdentifier>"));
    }
}
