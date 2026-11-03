package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Runs commands against a freshly started park and captures their output. */
final class ParkFixture {

    static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String PEDRO = "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    static final String LUIS = "USER CREATE;Luis;Perez Ruiz;11111111H;luis@correo.com;Clave1!;600000001;1111222233334444";
    static final String MARTA = "USER CREATE;Marta;Sanz Gil;22222222J;marta@correo.com;Clave1!;600000002;1111222233334445";

    final Park park;
    private final Path dir;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    ParkFixture(Path dir) throws IOException {
        this.dir = dir;
        this.park = Park.getInstance();
        restart();
    }

    /** Runs the commands and returns the output of the last one. */
    String run(String... commands) {
        String last = "";
        for (String command : commands) {
            int start = buffer.size();
            processor.process(command);
            last = buffer.toString(StandardCharsets.UTF_8).substring(start).strip();
        }
        return last;
    }

    /** Simulates a new execution on the same data directory. */
    void restart() throws IOException {
        buffer = new ByteArrayOutputStream();
        processor = TestParks.processor(new TextParkRepository(dir), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /** Creates {@code n} users with valid distinct data and returns their emails' prefix. */
    void createUsers(int n) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < n; i++) {
            int number = 30000000 + i;
            String dni = number + "" + letters.charAt(number % 23);
            run("USER CREATE;U" + i + ";Test;" + dni + ";u" + i + "@correo.com;Clave1!;600000000;1111222233334444");
        }
    }
}
