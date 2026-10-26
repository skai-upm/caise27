package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.CommandRunner;
import es.upm.parque.presentation.help.CommandCatalog;
import es.upm.parque.presentation.help.CommandInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the delivery command scripts through the application wiring used by {@link App}.
 */
class AppIntegrationTest {

    @TempDir
    Path dataDir;

    private List<String> runScript(CommandProcessor processor, String script) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandRunner runner = new CommandRunner(processor, new PrintStream(output, true, StandardCharsets.UTF_8));
        try (BufferedReader reader = Files.newBufferedReader(Path.of("examples", script), StandardCharsets.UTF_8)) {
            runner.run(reader);
        }
        List<String> lines = output.toString(StandardCharsets.UTF_8).lines().toList();
        for (String line : lines) {
            assertFalse(line.contains("Unknown command") || line.contains("Wrong number of parameters"),
                    script + ": " + line);
        }
        return lines;
    }

    @Test
    void deliveryScriptsRunTogetherAndStateSurvivesRestart() throws IOException {
        List<String> basic = runScript(App.buildProcessor(dataDir.resolve("d1")), "input_basico.txt");
        assertEquals("OK EXIT", basic.get(basic.size() - 1));

        List<String> phase2 = runScript(App.buildProcessor(dataDir.resolve("d2")), "input_fase2.txt");
        assertTrue(phase2.contains("OK GROUP TICKET CREATED -> 108.00"));
        assertTrue(phase2.contains("OK ATTRACTION ENTERED -> 3 users"));

        Path phase3Dir = dataDir.resolve("d3");
        List<String> phase3 = runScript(App.buildProcessor(phase3Dir), "input_fase3.txt");
        assertTrue(phase3.contains("OK SPEEDPASS CREATED -> 10 passes, 80.00"));
        assertTrue(phase3.contains("TOTAL: 232.00"));
        assertTrue(phase3.contains("OK ATTRACTION ENTERED -> 2 users, SpeedPasses consumed: TG01-1-SP1, TG01-2-SP1"));
        assertTrue(phase3.contains("OK ATTRACTION ENTERED -> 1 users, Special Pass consumed: T001-SPECIAL"));
        assertTrue(phase3.contains("ERROR Attraction A04 (SPECIAL) does not accept SPEED"));
        assertTrue(phase3.contains("OK TICKET DELETED"));
        assertTrue(phase3.contains("Available commands (extended):"));

        App.buildProcessor(phase3Dir);
        assertEquals(3, UserController.getInstance().getUsers().size());
        assertNull(TicketController.getInstance().find("T002"));
        assertTrue(PassController.getInstance().find("T001-SP1").isConsumed());
        assertTrue(PassController.getInstance().find("T001-SPECIAL").isConsumed());
        assertNull(PassController.getInstance().find("T002-SPECIAL"));
        assertEquals(AttractionType.SPECIAL, AttractionController.getInstance().find("A04").getType());
        assertTrue(AttractionController.getInstance().find("A04").getVisitors().isEmpty());
    }

    @Test
    void everyRegisteredCommandIsAcceptedAndDocumented() throws IOException {
        CommandProcessor processor = App.buildProcessor(dataDir);
        String help = processor.process("HELP");
        String extended = processor.process("HELP EXTENDED");
        for (CommandInfo command : CommandCatalog.standard().getCommands()) {
            assertTrue(help.contains(command.syntax()), command.operation());
            assertTrue(extended.contains(command.syntax() + System.lineSeparator() + "    " + command.description()),
                    command.operation());
            if (!command.operation().equals("EXIT")) {
                assertFalse(processor.process(command.operation() + ";x;y;z;w;v;u;t;s").contains("Unknown command"),
                        command.operation());
            }
        }
    }
}
