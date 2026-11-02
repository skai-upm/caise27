package es.upm.etsisi.amusementpark.presentation;

import static es.upm.etsisi.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.etsisi.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.persistence.FileRepositories;
import es.upm.etsisi.amusementpark.presentation.help.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.help.HelpStrategy;

class Delivery3CommandsTest {

    @TempDir
    Path dir;

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        park = ParkController.getInstance();
        park.configure(FileRepositories.in(dir));
        processor = App.wire(park);
        run("SYSTEM DATE;2027-04-15");
        run(CREATE_ANA);
        run(CREATE_PEDRO);
    }

    private String run(String line) {
        return processor.process(line).message();
    }

    @Test
    void helpListsEverySyntaxWithoutDescriptions() {
        String help = run("HELP");
        for (CommandSpec spec : CommandCatalog.ALL) {
            assertTrue(help.contains(spec.syntax()), spec.syntax());
            assertFalse(help.contains(spec.description()), spec.description());
        }
        for (String required : List.of("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>",
                "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP EXTENDED")) {
            assertTrue(help.contains(required), required);
        }
    }

    @Test
    void extendedHelpAddsDescriptions() {
        String help = run("HELP EXTENDED");
        for (CommandSpec spec : CommandCatalog.ALL) {
            assertTrue(help.contains(spec.syntax()), spec.syntax());
            assertTrue(help.contains(spec.description()), spec.description());
        }
    }

    @Test
    void helpGeneratorUsesInjectedStrategy() {
        HelpStrategy keywordsOnly = commands -> commands.stream().map(CommandSpec::keyword).distinct()
                .collect(Collectors.joining(","));
        String text = new HelpGenerator(keywordsOnly, CommandCatalog.ALL).generate();
        assertTrue(text.startsWith("USER CREATE,USER DELETE,USER UPDATE,SYSTEM DATE"));
    }

    @Test
    void processingContinuesAfterEachKindOfError() {
        assertTrue(run("NOPE").startsWith("ERROR:"));
        assertTrue(run("TICKET CREATE;T001;nobody@correo.com;2027-04-15").startsWith("ERROR:"));
        assertTrue(run("SYSTEM PRICE;-1").startsWith("ERROR:"));
        assertTrue(run("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR:"));
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
    }

    @Test
    void typedAttractionCreation() {
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL"));
        assertTrue(run("ATTRACTION CREATE;A05;X;5;VIP").startsWith("ERROR:"));
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(run("ATTRACTION STATUS;A04").contains("(SPECIAL)"));
    }

    @Test
    void passAndEntryCommands() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
        run("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        assertEquals("SPEEDPASS CREATED: SP02:1 to SP02:10. TOTAL PRICE: 80.00 EUR",
                run("SPEEDPASS CREATE;SP02;T001;10"));
        assertEquals("SPEEDPASS CREATED: SP01:1. TOTAL PRICE: 8.00 EUR", run("SPEEDPASS CREATE;SP01;T001;1"));
        assertTrue(run("SPEEDPASS CREATE;SP03;T001;5").startsWith("ERROR:"));
        assertEquals("SPECIALPASS CREATED. PRICE: 40.00 EUR", run("SPECIALPASS CREATE;PE01;T001"));
        assertTrue(run("ATTRACTION ENTER;A01;T001;SPEED").matches(
                "ATTRACTION ENTERED: 1 user\\(s\\)\\. SPEEDPASS CONSUMED: SP0[12]:\\d+"));
        assertTrue(run("ATTRACTION ENTER;A04;T001;SPEED").startsWith("ERROR:"));
        assertEquals("ATTRACTION ENTERED: 1 user(s). SPECIAL PASS CONSUMED: PE01", run("ATTRACTION ENTER;A04;T001"));
        assertTrue(run("SPEEDPASS DELETE;SP02:5").startsWith("ERROR:"));
    }

    @Test
    void refundCommands() {
        run("TICKET CREATE;T009;ana@correo.com;2027-04-20");
        run("SPEEDPASS CREATE;SP09;T009;10");
        run("SPECIALPASS CREATE;PE09;T009");
        assertEquals("SPEEDPASS DELETED", run("SPEEDPASS DELETE;SP09:3"));
        assertEquals("SPECIALPASS DELETED", run("SPECIALPASS DELETE;PE09"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T009"));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void invoiceCommands() {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SPEEDPASS CREATE;SP01;TG01:12345678Z;1");
        String user = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(user.startsWith("USER INVOICE"));
        assertTrue(user.contains("Ticket T001"));
        assertTrue(user.contains("SpeedPass SP01:1 for ticket TG01:12345678Z: 8.00 EUR"));
        assertTrue(user.endsWith("TOTAL: 48.00 EUR"));
        assertFalse(user.contains("Special Passes"));
        String group = run("INVOICE CREATE;GROUP;G01");
        assertTrue(group.startsWith("GROUP INVOICE"));
        assertTrue(group.contains("TG01:87654321X"));
        assertTrue(group.endsWith("TOTAL: 80.00 EUR"));
        assertTrue(run("INVOICE CREATE;SHOP;x").startsWith("ERROR:"));
        assertTrue(run("INVOICE CREATE;USER;nobody@correo.com").startsWith("ERROR:"));
        assertTrue(run("INVOICE CREATE;GROUP;G99").startsWith("ERROR:"));
    }
}
