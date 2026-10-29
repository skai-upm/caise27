package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class AppTest {

    static final List<String> SCRIPT = List.of(
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Eva;Ruiz;11111111H;eva@correo.com;Clave1!;600000000;1111222233334444");

    static final String ANA_LINE =
            "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path workDir) {
        return App.run(args, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8),
                workDir);
    }

    private Path script() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, SCRIPT);
        return input;
    }

    @Test
    void fileModeAndInteractiveModeBehaveTheSame() throws Exception {
        Path input = script();
        Path fileWork = Files.createDirectory(dir.resolve("file"));
        assertEquals(0, run(new String[] {input.toString()}, "", fileWork));
        String fileOutput = out.toString(StandardCharsets.UTF_8);
        out.reset();

        Path interactiveWork = Files.createDirectory(dir.resolve("interactive"));
        assertEquals(0, run(new String[0], String.join("\n", SCRIPT) + "\n", interactiveWork));
        String interactiveOutput = out.toString(StandardCharsets.UTF_8);

        assertEquals(fileOutput, interactiveOutput);
        assertTrue(fileOutput.startsWith("USER CREATED"));
        List<String> fileUsers = Files.readAllLines(fileWork.resolve("users.txt"));
        List<String> interactiveUsers = Files.readAllLines(interactiveWork.resolve("users.txt"));
        assertEquals(1, fileUsers.size());
        assertEquals(1, interactiveUsers.size());
        ParkFixture.assertStoredUser(ANA_LINE, fileUsers.get(0));
        ParkFixture.assertStoredUser(ANA_LINE, interactiveUsers.get(0));
    }

    @Test
    void missingCommandFileIsReportedWithoutException() {
        int status = run(new String[] {dir.resolve("missing.txt").toString()}, "", dir);
        assertEquals(1, status);
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void directoryAsCommandFileIsReportedWithoutException() {
        assertEquals(1, run(new String[] {dir.toString()}, "", dir));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }

    @Test
    void flyWithFileLeavesNoDataAndKeepsNormalData() throws Exception {
        Path work = Files.createDirectory(dir.resolve("work"));
        Files.write(work.resolve("users.txt"), List.of(
                "Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"));
        Path input = script();

        assertEquals(0, run(new String[] {App.FLY, input.toString()}, "", work));

        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("USER CREATED"));
        assertFalse(Files.exists(work.resolve("tmp")));
        assertEquals(List.of("Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444"),
                Files.readAllLines(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("groups.txt")));
    }

    @Test
    void flyInteractiveStartsEmptyAndCleansUp() throws Exception {
        Path work = Files.createDirectory(dir.resolve("work"));
        Files.write(work.resolve("users.txt"), List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));

        assertEquals(0, run(new String[] {App.FLY}, CommandProcessorTest.CREATE_ANA + "\nEXIT\n", work));

        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("USER CREATED"));
        assertFalse(Files.exists(work.resolve("tmp")));
    }

    @Test
    void flyDirectoryIsTimestampedInsideTmp() throws Exception {
        Path tmpRoot = dir.resolve("tmp");
        Path first = App.createFlyDirectory(tmpRoot);
        Path second = App.createFlyDirectory(tmpRoot);
        assertEquals(tmpRoot, first.getParent());
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        assertFalse(first.equals(second));
    }

    @Test
    void flyKeepsTemporaryDataOnlyForTheRun() throws Exception {
        Path work = Files.createDirectory(dir.resolve("work"));
        Path input = dir.resolve("fly.txt");
        Files.write(input, List.of(CommandProcessorTest.CREATE_ANA, CommandProcessorTest.CREATE_ANA));
        assertEquals(0, run(new String[] {App.FLY, input.toString()}, "", work));
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.startsWith("USER CREATED"));
        assertTrue(output.contains("ERROR:"));
        assertFalse(Files.exists(work.resolve("users.txt")));
        assertFalse(Files.exists(work.resolve("tmp")));
    }
}
