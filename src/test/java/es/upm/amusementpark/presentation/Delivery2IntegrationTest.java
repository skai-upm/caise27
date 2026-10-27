package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
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
 * End-to-end checks of the complete Delivery 2 feature set together with the
 * Delivery 1 commands.
 */
class Delivery2IntegrationTest {

    private static final String SESSION = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave3$;600000000;1111222233334444",
            "SYSTEM DATE;2027-04-01",
            "SYSTEM PRICE;45.50",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;luis@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUP ADD;G01;luis@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;1",
            "SYSTEM DATE;2027-04-15",
            "TICKET DELETE;T001",
            "USER DELETE;luis@correo.com;Clave3$",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION ENTER;A01;T001",
            "ATTRACTION EXIT;A01;ana@correo.com",
            "ATTRACTION STATUS;A01",
            "ATTRACTION LIST;A01",
            "EXIT",
            "");

    private static final List<String> EXPECTED_SUCCESS = List.of(
            "USER CREATED",
            "USER CREATED",
            "USER CREATED",
            "SYSTEM DATE UPDATED: 2027-04-01",
            "SYSTEM PRICE UPDATED: 45.50",
            "GROUP CREATED",
            "USER ADDED TO GROUP",
            "USER ADDED TO GROUP",
            "TICKET CREATED. PRICE: 45.50 EUR",
            "GROUP TICKET CREATED. TOTAL PRICE: 91.00 EUR",
            "ERROR",
            "ATTRACTION CREATED",
            "ATTRACTION CREATED",
            "SYSTEM DATE UPDATED: 2027-04-15",
            "ERROR",
            "ERROR",
            "ERROR",
            "ENTERED ATTRACTION A01: ana@correo.com, pedro@correo.com",
            "ENTERED ATTRACTION A01: luis@correo.com",
            "EXITED ATTRACTION A01: ana@correo.com",
            "ATTRACTION A01 - Dragon Rojo",
            "Capacity: 20",
            "Users inside: 2",
            "Available places: 18",
            "Inside: pedro@correo.com, luis@correo.com",
            "pedro@correo.com",
            "luis@correo.com",
            "Exiting application.");

    @TempDir
    Path tempDir;

    private String run(String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(new byte[0]), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                tempDir.resolve("data"), tempDir.resolve("tmp"));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void assertSession(String output) {
        List<String> lines = List.of(output.split(System.lineSeparator()));
        assertEquals(EXPECTED_SUCCESS.size(), lines.size(), output);
        for (int i = 0; i < lines.size(); i++) {
            if (EXPECTED_SUCCESS.get(i).equals("ERROR")) {
                assertTrue(lines.get(i).startsWith("ERROR"), lines.get(i));
            } else {
                assertEquals(EXPECTED_SUCCESS.get(i), lines.get(i));
            }
        }
    }

    @Test
    void completeSessionFromFile() throws IOException {
        Path input = tempDir.resolve("input_fase2.txt");
        Files.writeString(input, SESSION, StandardCharsets.UTF_8);
        assertSession(run(input.toString()));
        assertTrue(Files.readString(tempDir.resolve("data/tickets.txt")).contains("TG01"));
    }

    @Test
    void completeSessionWithFlyLeavesNoData() throws IOException {
        Path input = tempDir.resolve("input_fase2.txt");
        Files.writeString(input, SESSION, StandardCharsets.UTF_8);
        assertSession(run("--fly", input.toString()));
        assertFalse(Files.exists(tempDir.resolve("tmp")));
        assertFalse(Files.exists(tempDir.resolve("data")));
    }
}
