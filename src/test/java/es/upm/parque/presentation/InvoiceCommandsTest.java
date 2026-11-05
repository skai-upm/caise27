package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.ParkController;
import es.upm.parque.persistence.ParkFileStore;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.persistence.UserRepository;

class InvoiceCommandsTest {

    @TempDir
    Path dir;

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkController.getInstance().startExecution(users, new ParkFileStore(dir));
        processor = new CommandProcessor();
        run("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "USER CREATE;Luis;Martin;11111111H;luis@correo.com;Clave3$;600000002;1234567890123458",
                "USER CREATE;Eva;Ruiz;22222222J;eva@correo.com;Clave4%;600000003;1234567890123459",
                "SYSTEM DATE;2027-04-01",
                "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUP ADD;G01;eva@correo.com",
                "GROUP CREATE;G02;Amigos",
                "TICKET CREATE;T2;ana@correo.com;2027-04-20",
                "TICKET CREATE;T1;ana@correo.com;2027-04-15",
                "GROUPTICKET CREATE;GT1;G01;2027-04-18");
    }

    private void run(String... lines) {
        for (String line : lines) {
            processor.process(line);
        }
    }

    @Test
    void userInvoiceListsDirectTicketsOnlyWithTotal() {
        String r = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(r.startsWith("OK: Invoice for user ana@correo.com"), r);
        assertTrue(r.contains("Name: Ana Garcia") && r.contains("DNI: 12345678Z") && r.contains("Email: ana@correo.com"), r);
        assertTrue(r.contains("Individual tickets:"), r);
        assertTrue(r.indexOf("Ticket T1 for 2027-04-15  40.00 EUR") < r.indexOf("Ticket T2 for 2027-04-20  40.00 EUR"), r);
        assertFalse(r.contains("GT1"), r);
        assertTrue(r.endsWith("TOTAL: 80.00 EUR"), r);
    }

    @Test
    void userWithoutPurchasesHasNoEmptySections() {
        String r = processor.process("INVOICE CREATE;USER;pedro@correo.com");
        assertTrue(r.startsWith("OK"), r);
        assertFalse(r.contains("Individual tickets"), r);
        assertTrue(r.endsWith("TOTAL: 0.00 EUR"), r);
    }

    @Test
    void groupInvoiceCountsEachGroupTicketOnce() {
        String r = processor.process("INVOICE CREATE;GROUP;G01");
        assertTrue(r.startsWith("OK: Invoice for group G01"), r);
        assertTrue(r.contains("Group: G01") && r.contains("Name: Familia"), r);
        assertTrue(r.contains("Group ticket GT1 for 2027-04-18 (4 tickets, discount 10%)  144.00 EUR"), r);
        assertTrue(r.contains("- Ana Garcia (12345678Z) - ticket GT1:12345678Z, 36.00 EUR"), r);
        assertEquals(4, r.split("ticket GT1:", -1).length - 1, r);
        assertFalse(r.contains("Ticket T1 for") || r.contains("Ticket T2 for"), r);
        assertTrue(r.endsWith("TOTAL: 144.00 EUR"), r);
    }

    @Test
    void groupWithoutPurchases() {
        String r = processor.process("INVOICE CREATE;GROUP;G02");
        assertTrue(r.contains("Name: Amigos") && !r.contains("Group tickets"), r);
        assertTrue(r.endsWith("TOTAL: 0.00 EUR"), r);
    }

    @Test
    void unknownRecipientsAndBadSyntaxAreRejected() {
        assertTrue(processor.process("INVOICE CREATE;USER;nobody@correo.com").startsWith("ERROR: no user"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G99").startsWith("ERROR: no group"));
        String bad = processor.process("INVOICE CREATE;COMPANY;X1");
        assertTrue(bad.startsWith("ERROR") && bad.contains("INVOICE CREATE;USER;<email>")
                && bad.contains("INVOICE CREATE;GROUP;<groupIdentifier>"), bad);
        assertTrue(processor.process("INVOICE CREATE;USER").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;USER;").startsWith("ERROR"));
        assertTrue(processor.process("INVOICE CREATE;USER;ana@correo.com;x").startsWith("ERROR"));
    }

    @Test
    void generatingInvoicesDoesNotChangeTheDataFiles() throws IOException {
        List<String> before = snapshot();
        processor.process("INVOICE CREATE;USER;ana@correo.com");
        processor.process("INVOICE CREATE;GROUP;G01");
        assertEquals(before, snapshot());
    }

    private List<String> snapshot() throws IOException {
        List<String> content = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                content.add(file.getFileName() + "=" + Files.readString(file));
            }
        }
        return content;
    }
}
