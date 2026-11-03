package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TicketCommandsTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(CommandProcessorTest.CREATE_ANA);
        p.process("SYSTEM DATE;2026-10-01");
    }

    @Test
    void sourceExample() {
        assertEquals("OK TICKET CREATED -> 40.00", p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK:"));
    }

    @Test
    void wrongPasswordIsReportedBeforeTicketCheck() {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("ERROR: Incorrect password", p.process("USER DELETE;ana@correo.com;Mala1!"));
    }

    @Test
    void priceChangesOnlyAffectLaterTickets() {
        assertEquals("OK TICKET CREATED -> 40.00", p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertEquals("OK: Ticket price set to 60.00", p.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", p.process("TICKET CREATE;T002;ana@correo.com;2026-10-20"));
        assertEquals("OK: Ticket price set to 55.50", p.process("SYSTEM PRICE;55.5"));
        assertEquals("OK TICKET CREATED -> 55.50", p.process("TICKET CREATE;T003;ana@correo.com;2026-10-20"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;abc", "SYSTEM PRICE;-5", "SYSTEM PRICE;10.999", "SYSTEM PRICE", "SYSTEM PRICE;1;2"})
    void invalidPriceIsRejected(String command) {
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertEquals(CommandProcessor.DEFAULT_PRICE, p.getCurrentPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "TICKET CREATE;T009;nadie@correo.com;2026-10-15", "TICKET CREATE;T009;ana@correo.com;2026-09-30",
        "TICKET CREATE;T009;ana@correo.com;2026-02-30", "TICKET CREATE;T009;ana@correo.com", "TICKET DELETE",
        "TICKET DELETE;T999"})
    void invalidTicketCommandsAreRejected(String command) {
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertTrue(p.process("TICKET DELETE;T009").startsWith("ERROR:"));
    }

    @Test
    void ticketDeletionBoundaries() {
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("TICKET DELETE;T001").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-14");
        assertEquals("OK: Ticket T001 deleted", p.process("TICKET DELETE;T001"));
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK:"));
    }

    @Test
    void helpListsThirteenCommands() {
        String help = p.process("HELP");
        assertTrue(CommandProcessor.COMMAND_SYNTAX.size() >= 13);
        for (String syntax : CommandProcessor.COMMAND_SYNTAX) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertTrue(help.contains("TICKET CREATE;Id;Correo;yyyy-MM-dd"));
        assertTrue(help.contains("SYSTEM PRICE;Precio"));
    }
}
