package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.park.Park;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    @TempDir
    Path dir;

    @Test
    void stopsAtExitAndIgnoresBlankLines() throws Exception {
        CommandProcessor processor = TestSupport.start(dir);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new CommandRunner(processor, new PrintStream(bytes, true, StandardCharsets.UTF_8))
                .run(new BufferedReader(new StringReader(String.join("\n", TestSupport.ANA, "   ", "", "EXIT",
                        TestSupport.PEDRO))));
        assertEquals(1, Park.getInstance().users().size());
        assertEquals(List.of("USER CREATED", "BYE"), bytes.toString(StandardCharsets.UTF_8).lines().toList());
    }
}
