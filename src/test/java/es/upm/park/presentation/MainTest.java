package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private Path usersFile;

    @BeforeEach
    void useTemporaryUsersFile() {
        usersFile = dir.resolve("users.txt");
        System.setProperty(Main.DATA_DIR_PROPERTY, dir.toString());
        System.setProperty(Main.TMP_DIR_PROPERTY, dir.resolve("tmp").toString());
    }

    @AfterEach
    void clearProperty() {
        System.clearProperty(Main.DATA_DIR_PROPERTY);
        System.clearProperty(Main.TMP_DIR_PROPERTY);
    }

    private String run(String input, String... args) {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Main.main(args);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
    }

    /** Users file lines with the BCrypt password field replaced by {@code <hash>}. */
    private List<String> storedUsers() throws java.io.IOException {
        return Files.readAllLines(usersFile).stream().map(MainTest::maskHash).toList();
    }

    private static String maskHash(String line) {
        String[] f = line.split(";", -1);
        assertTrue(f[4].startsWith("$2a$"), "password not protected: " + line);
        f[4] = "<hash>";
        return String.join(";", f);
    }

    private static String withHash(String line) {
        String[] f = line.split(";", -1);
        f[4] = "<hash>";
        return String.join(";", f);
    }

    @Test
    void startsWithoutArguments() {
        assertEquals(Main.BANNER, run(""));
    }

    @Test
    void missingCommandFileIsReportedInAControlledWay() {
        Path missing = dir.resolve("missing.txt");
        assertEquals(Main.BANNER + "\nError: cannot read command file " + missing, run("", missing.toString()));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        assertEquals(Main.BANNER + "\nError: cannot read command file " + dir, run("", dir.toString()));
    }

    @Test
    void commandFileIsProcessedInOrderWithoutReadingStandardInput() throws Exception {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, List.of(ANA, "", "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        String out = run("USER DELETE;ana@correo.com;Clave1!\n", commands.toString());
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nUser updated: 12345678Z PHONE", out);
        assertEquals("699999999", Files.readAllLines(usersFile).get(0).split(";")[5]);
    }

    @Test
    void sameOutputAndEffectsFromFileAndStandardInput() throws Exception {
        String script = ANA + "\nFOO\nUSER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\nUSER DELETE;x@y.com;Clave1!\n";
        String fromStdin = run(script);
        List<String> storedFromStdin = storedUsers();
        Files.delete(usersFile);
        Path commands = dir.resolve("same.txt");
        Files.writeString(commands, script);
        assertEquals(fromStdin, run("", commands.toString()));
        assertEquals(storedFromStdin, storedUsers());
    }

    @Test
    void processesStandardInputInOrder() {
        String input = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "\n"
                + "FOO BAR;1\n"
                + "USER CREATE;Luis;Perez;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444\n";
        String[] lines = run(input).split("\n");
        assertEquals(4, lines.length);
        assertEquals(Main.BANNER, lines[0]);
        assertEquals("User created: 12345678Z", lines[1]);
        assertEquals("Error: unknown command 'FOO BAR'", lines[2]);
        assertEquals("Error: a user with DNI 12345678Z already exists", lines[3]);
    }

    @Test
    void usersSurviveRestart() throws Exception {
        assertEquals(Main.BANNER + "\nUser created: 12345678Z", run(ANA + "\n"));
        assertEquals(List.of(withHash(ANA.substring("USER CREATE;".length()))), storedUsers());
        assertEquals(Main.BANNER + "\nError: a user with DNI 12345678Z already exists", run(ANA + "\n"));
        assertEquals(1, Files.readAllLines(usersFile).size());
    }

    @Test
    void updatesSurviveRestart() throws Exception {
        run(ANA + "\nUSER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\n");
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456"),
                storedUsers());
        assertEquals(Main.BANNER + "\nUser updated: 12345678Z PHONE", run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699\n"));
        assertEquals("Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;<hash>;699;1234567890123456",
                storedUsers().get(0));
    }

    @Test
    void noCommandRunsAfterExitInFileMode() throws Exception {
        Path commands = dir.resolve("exit.txt");
        Files.write(commands, List.of(ANA, "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nBye", run("", commands.toString()));
        assertEquals(1, Files.readAllLines(usersFile).size());
    }

    @Test
    void noCommandRunsAfterExitInInteractiveMode() {
        assertEquals(Main.BANNER + "\nBye", run("EXIT\nHELP\n"));
    }

    @Test
    void helpIsIdenticalInBothModes() throws Exception {
        Path commands = dir.resolve("help.txt");
        Files.write(commands, List.of("HELP"));
        String fromStdin = run("HELP\n");
        assertTrue(fromStdin.contains("USER DELETE;<email>;<password>"));
        assertEquals(fromStdin, run("", commands.toString()));
    }

    private static final List<String> SECTION_16 = List.of(
            ANA,
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @Test
    void deliveryOneSequenceIsEquivalentInBothModes() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, SECTION_16);
        String fromFile = run("", input.toString());
        List<String> storedFromFile = storedUsers();
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;<hash>;699999999;1234567890123456"),
                storedFromFile);
        assertTrue(fromFile.contains("User created: 87654321X\nUser updated: 12345678Z PHONE\n"
                + "User updated: 12345678Z EMAIL\nUser deleted: pedro@correo.com\nAvailable commands:"));
        assertTrue(fromFile.endsWith("EXIT\nBye"));
        Files.delete(usersFile);
        assertEquals(fromFile, run(String.join("\n", SECTION_16) + "\n"));
        assertEquals(storedFromFile, storedUsers());
    }

    @Test
    void incorrectLinesAreReportedAndValidOnesApplied() throws Exception {
        Path input = dir.resolve("mixed.txt");
        Files.write(input, List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456",
                ANA,
                ANA,
                "USER DELETE;ana@correo.com;wrong",
                "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
                "NOPE",
                ";;;",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;611"));
        String out = run("", input.toString());
        assertEquals(String.join("\n", Main.BANNER,
                "Error: invalid DNI",
                "User created: 12345678Z",
                "Error: a user with DNI 12345678Z already exists",
                "Error: incorrect password",
                "Error: field DNI cannot be modified",
                "Error: unknown command 'NOPE'",
                "Error: unknown command ''",
                "Error: USER UPDATE expects 4 fields",
                "User updated: 12345678Z PHONE"), out);
        assertEquals("611", Files.readAllLines(usersFile).get(0).split(";")[5]);
    }

    @Test
    void unreadableUsersFileStopsInAControlledWay() throws Exception {
        Files.createDirectory(usersFile);
        String out = run(ANA + "\n");
        assertEquals(Main.BANNER + "\nError: cannot load park data: cannot read " + usersFile, out);
        assertTrue(Files.isDirectory(usersFile));
    }

    @Test
    void priceRevertsOnNewExecutionAndDateStartsToday() {
        run("SYSTEM PRICE;45.50\nSYSTEM DATE;2030-01-01\n");
        run("");
        assertEquals(new java.math.BigDecimal("40.00"), es.upm.park.business.controller.ParkController.getInstance().getBasePrice());
        assertEquals(java.time.LocalDate.now(), es.upm.park.business.controller.ParkController.getInstance().getCurrentDate());
    }

    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    private boolean tmpIsEmpty() throws Exception {
        Path tmp = dir.resolve("tmp");
        try (var children = Files.list(tmp)) {
            return children.findAny().isEmpty();
        }
    }

    @Test
    void parkStateSurvivesRestart() throws Exception {
        run(ANA + "\n" + PEDRO + "\nSYSTEM DATE;2027-04-15\nGROUP CREATE;G01;Familia Garcia\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUP ADD;G01;pedro@correo.com\nATTRACTION CREATE;A01;Dragon Rojo;20\n"
                + "TICKET CREATE;T001;ana@correo.com;2099-04-15\nGROUPTICKET CREATE;TG01;G01;2099-04-15\n");
        String out = run("GROUP ADD;G01;ana@correo.com\nTICKET CREATE;T001;pedro@correo.com;2099-04-16\n"
                + "ATTRACTION CREATE;A01;X;3\nATTRACTION LIST;A01\nGROUP DELETE;G01\n");
        assertEquals(String.join("\n", Main.BANNER,
                "Error: group G01 has an active group ticket",
                "Error: ticket T001 already exists",
                "Error: attraction A01 already exists",
                "Attraction A01 has no users inside",
                "Error: group G01 has an active group ticket"), out);
        assertTrue(Files.readString(dir.resolve("tickets.txt")).contains("MEMBER;87654321X;Pedro;Lopez Martin;pedro@correo.com"));
    }

    @Test
    void flyModeWithFileLeavesPermanentDataUntouched() throws Exception {
        run(ANA + "\n");
        List<String> permanent = Files.readAllLines(usersFile);
        Path input = dir.resolve("fly.txt");
        Files.write(input, List.of(ANA, PEDRO, "GROUP CREATE;G01;F"));
        String out = run("", "--fly", input.toString());
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nUser created: 87654321X\nGroup created: G01", out);
        assertEquals(permanent, Files.readAllLines(usersFile));
        assertTrue(Files.notExists(dir.resolve("groups.txt")));
        assertTrue(tmpIsEmpty());
    }

    @Test
    void flyModeInteractiveIsCleanedOnExitAndOnEndOfInput() throws Exception {
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nBye", run(ANA + "\nEXIT\n", "--fly"));
        assertTrue(tmpIsEmpty());
        assertEquals(Main.BANNER + "\nUser created: 12345678Z", run(ANA + "\n", "--fly"));
        assertTrue(tmpIsEmpty());
        assertTrue(Files.notExists(usersFile));
    }

    private static final List<String> DELIVERY_2 = List.of(
            ANA, PEDRO,
            "USER CREATE;Luis;Martin;00000000T;luis@correo.com;Clave1!;600000000;1111222233334444",
            "SYSTEM DATE;2027-04-15",
            "SYSTEM PRICE;45.50",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUP REMOVE;G01;luis@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;2",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;ana@correo.com",
            "ATTRACTION ENTER;A02;T001",
            "ATTRACTION LIST;A01",
            "ATTRACTION STATUS;A02",
            "TICKET DELETE;T001",
            "USER DELETE;luis@correo.com;Clave1!",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "HELP",
            "EXIT",
            "USER CREATE;Nadie;X;87654321X;n@correo.com;Clave1!;6;1234567890123456");

    @Test
    void deliveryTwoFileGivesSameResultsWithAndWithoutFly() throws Exception {
        Path input = dir.resolve("input_fase2.txt");
        Files.write(input, DELIVERY_2);
        String fly = run("", "--fly", input.toString());
        assertTrue(tmpIsEmpty());
        assertTrue(Files.notExists(usersFile));
        String normal = run("", input.toString());
        assertEquals(fly, normal);
        for (String expected : new String[] {
            "Group ticket created: TG01 total 122.85",
            "Error: group G01 has an active group ticket",
            "Error: attraction A02 does not have capacity for 3 users",
            "Entered A01: ana@correo.com, pedro@correo.com, luis@correo.com",
            "Exited A01: ana@correo.com",
            "Entered A02: ana@correo.com",
            "Users in A01:\npedro@correo.com\nluis@correo.com",
            "Attraction A02 - Torre del Cielo\nCapacity: 2\nInside: 1\nAvailable: 1\nUsers: ana@correo.com",
            "Error: ticket T001 cannot be deleted: it is not dated after 2027-04-15",
            "Error: user luis@correo.com has an active ticket",
            "User updated: 12345678Z PHONE"}) {
            assertTrue(normal.contains(expected), expected);
        }
        for (String command : new String[] {"USER CREATE;", "USER DELETE;", "USER UPDATE;", "SYSTEM DATE;", "SYSTEM PRICE;",
            "GROUP CREATE;", "GROUP ADD;", "GROUP REMOVE;", "GROUP DELETE;", "TICKET CREATE;", "TICKET DELETE;",
            "GROUPTICKET CREATE;", "GROUPTICKET DELETE;", "ATTRACTION CREATE;", "ATTRACTION ENTER;", "ATTRACTION EXIT;",
            "ATTRACTION LIST;", "ATTRACTION STATUS;", "  HELP", "  EXIT"}) {
            assertTrue(normal.contains(command), command);
        }
        assertTrue(normal.endsWith("Bye"));
        assertTrue(Files.readString(dir.resolve("groups.txt")).startsWith("G01;Familia Garcia;12345678Z,87654321X,00000000T"));
    }

    @Test
    void failingOperationsDoNotStopProcessing() throws Exception {
        Path input = dir.resolve("errors.txt");
        Files.write(input, List.of("NOT A COMMAND", "GROUP ADD;G99;ana@correo.com", ANA));
        assertEquals(String.join("\n", Main.BANNER, "Error: unknown command 'NOT A COMMAND'", "Error: no group G99",
                "User created: 12345678Z"), run("", input.toString()));
    }
}
