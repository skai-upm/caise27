package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs a session combining the commands of both deliveries.
 */
class DeliveryTwoIntegrationTest {

    static final List<String> HELP_SYNTAX = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT");

    static final String SESSION = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Perez Gil;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana.nueva@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "SYSTEM PRICE;50.00",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SYSTEM PRICE;45.50",
            "TICKET CREATE;T001;luis@correo.com;2027-04-16",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;2",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;pedro@correo.com",
            "ATTRACTION STATUS;A01",
            "GROUP REMOVE;G01;luis@correo.com",
            "TICKET DELETE;TG01:11111111H",
            "USER DELETE;luis@correo.com;Luis3$",
            "TICKET DELETE;T001",
            "HELP",
            "EXIT",
            "USER CREATE;Nadie;X;22222222J;nadie@correo.com;Nadie4%;6;1234123412341234");

    @TempDir
    Path dir;

    private String run(Path data, String stdin, String... args) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        assertEquals(0, App.execute(args, new BufferedReader(new StringReader(stdin)), out, out, data, dir.resolve("tmp")));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void combinedSessionFromFile() throws Exception {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, SESSION);
        Path data = dir.resolve("data");
        String out = run(data, "", input.toString());
        List<String> l = List.of(out.split("\\R"));
        assertEquals("GROUPTICKET CREATED. Total price: 135.00 EUR", l.get(10));
        assertEquals("TICKET CREATED. Price: 45.50 EUR", l.get(12));
        assertTrue(l.get(15).startsWith("ERROR: Not enough capacity"));
        assertEquals("USER ENTERED ATTRACTION: ana.nueva@correo.com", l.get(16));
        assertTrue(out.contains("USER LEFT ATTRACTION: pedro@correo.com"));
        assertTrue(out.contains("Occupants: 2"));
        assertTrue(out.contains("Available: 18"));
        assertTrue(out.contains("ERROR: Group is locked"));
        assertTrue(out.contains("ERROR: Ticket belongs to a group ticket"));
        assertTrue(out.contains("ERROR: User has active tickets"));
        assertTrue(out.contains("TICKET DELETED"));
        HELP_SYNTAX.forEach(s -> assertTrue(out.contains(s), s));
        assertEquals("Goodbye", l.get(l.size() - 1));
        assertTrue(Files.readAllLines(data.resolve("users.txt")).stream().noneMatch(u -> u.contains("nadie")));

        String restarted = run(data, "SYSTEM DATE;2027-04-15\nATTRACTION LIST;A01\nGROUP REMOVE;G01;luis@correo.com");
        assertTrue(restarted.contains("NO USERS IN ATTRACTION A01"));
        assertTrue(restarted.contains("ERROR: Group is locked"));
    }

    @Test
    void combinedSessionInFlyModeLeavesNoData() throws Exception {
        Path data = dir.resolve("data");
        String out = run(data, SESSION, "--fly");
        assertTrue(out.contains("GROUPTICKET CREATED. Total price: 135.00 EUR"));
        assertTrue(Files.notExists(data.resolve("users.txt")));
        try (var left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
