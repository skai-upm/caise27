package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InvoiceCommandsTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        p.process(TestData.CREATE_PEDRO);
        p.process(TestData.CREATE_LUIS);
        p.process("GROUP CREATE;G01;Familia Garcia");
        p.process("GROUP ADD;G01;ana@correo.com");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("GROUP ADD;G01;luis@correo.com");
        p.process("SYSTEM DATE;2026-10-01");
    }

    private List<String> invoice(String command) {
        return p.process(command).lines().toList();
    }

    @Test
    void userInvoiceListsDirectTicketsWithCapturedPrices() {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("SYSTEM PRICE;60.00");
        p.process("TICKET CREATE;T002;ana@correo.com;2026-10-16");
        assertEquals(List.of("INVOICE USER ana@correo.com (Ana Garcia Lopez) - 2026-10-01", "Individual tickets:",
                "  T001 2026-10-15 40.00", "  T002 2026-10-16 60.00", "Total: 100.00"),
                invoice("INVOICE CREATE;USER;ana@correo.com"));
    }

    @Test
    void groupTicketIsNotChargedToMembers() {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        List<String> ana = invoice("INVOICE CREATE;USER;ana@correo.com");
        assertEquals("Total: 40.00", ana.get(ana.size() - 1));
        assertFalse(ana.stream().anyMatch(l -> l.contains("TG01")));
        assertEquals(List.of("INVOICE GROUP G01 (Familia Garcia) - 2026-10-01", "Group tickets:",
                "  TG01 2026-10-15 3 members, 10% discount 108.00", "Total: 108.00"),
                invoice("INVOICE CREATE;GROUP;G01"));
    }

    @Test
    void emptySectionsAreOmitted() {
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertEquals(List.of("INVOICE USER pedro@correo.com (Pedro Lopez Martin) - 2026-10-01", "Total: 0.00"),
                invoice("INVOICE CREATE;USER;pedro@correo.com"));
        p.process("GROUP CREATE;G02;Vacio");
        assertEquals(List.of("INVOICE GROUP G02 (Vacio) - 2026-10-01", "Total: 0.00"), invoice("invoice create;group;G02"));
    }

    @Test
    void historicalTicketsStayOnTheInvoice() {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        p.process("SYSTEM DATE;2026-12-01");
        assertTrue(invoice("INVOICE CREATE;USER;ana@correo.com").contains("  T001 2026-10-01 40.00"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99",
        "INVOICE CREATE;PERSON;ana@correo.com", "INVOICE CREATE;USER", "INVOICE CREATE;USER;ana@correo.com;x"})
    void invalidRequestsAreRejected(String command) {
        assertTrue(p.process(command).startsWith("ERROR:"), command);
    }

    @Test
    void invoicesAreNotPersisted() throws IOException {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        List<String> before = listing();
        p.process("INVOICE CREATE;USER;ana@correo.com");
        p.process("INVOICE CREATE;GROUP;G01");
        assertEquals(before, listing());
    }

    private List<String> listing() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(f -> {
                try {
                    return f.getFileName() + ":" + Files.size(f);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }).sorted().toList();
        }
    }

    @Test
    void helpListsTheInvoiceCommands() {
        String help = p.process("HELP");
        assertEquals(21, CommandProcessor.COMMAND_SYNTAX.size());
        assertTrue(help.contains("INVOICE CREATE;USER;<correo>"));
        assertTrue(help.contains("INVOICE CREATE;GROUP;<identificadorGrupo>"));
    }
}
