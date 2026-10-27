package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CommandRunnerTest {

    /** Records every processed line while keeping the real HELP/EXIT behaviour. */
    private static class RecordingProcessor extends CommandProcessor {
        final List<String> processed = new ArrayList<>();

        RecordingProcessor() {
            super(new PrintStream(new ByteArrayOutputStream()));
        }

        @Override
        public boolean process(String line) {
            processed.add(line);
            return super.process(line);
        }
    }

    @Test
    void commandsAfterExitAreNotProcessed() throws IOException {
        RecordingProcessor processor = new RecordingProcessor();
        new CommandRunner(processor).run(new BufferedReader(new StringReader("HELP\nEXIT\nHELP\n")));
        assertEquals(List.of("HELP", "EXIT"), processor.processed);
    }

    @Test
    void inputWithoutExitEndsAfterLastCommand() throws IOException {
        RecordingProcessor processor = new RecordingProcessor();
        new CommandRunner(processor).run(new BufferedReader(new StringReader("HELP\nHELP\n")));
        assertEquals(List.of("HELP", "HELP"), processor.processed);
    }
}
