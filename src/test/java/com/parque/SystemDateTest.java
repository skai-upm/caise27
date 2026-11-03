package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemDateTest {

    @TempDir
    Path dir;
    UserService users;
    CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(users, LocalDate.of(2026, 9, 15));
    }

    @Test
    void startsWithOperatingSystemDate() {
        LocalDate before = LocalDate.now();
        CommandProcessor fresh = new CommandProcessor(users);
        LocalDate after = LocalDate.now();
        LocalDate date = fresh.getCurrentDate();
        assertTrue(!date.isBefore(before) && !date.isAfter(after), date.toString());
    }

    @Test
    void setsValidDate() {
        CommandProcessor fresh = new CommandProcessor(users);
        assertEquals("OK: System date set to 2026-09-15", fresh.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), fresh.getCurrentDate());
    }

    @Test
    void movesBackwardAndForward() {
        assertTrue(processor.process("SYSTEM DATE;2025-01-01").startsWith("OK: "));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2030-12-31").startsWith("OK: "));
        assertEquals(LocalDate.of(2030, 12, 31), processor.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026/09/15", "SYSTEM DATE;2026-9-15",
            "SYSTEM DATE;hoy", "SYSTEM DATE;2026-02-30", "SYSTEM DATE;2026-13-01", "SYSTEM DATE;2026-09-15 ",
            "SYSTEM DATE;", "SYSTEM DATE", "SYSTEM DATE;2026-09-16;extra", "SYSTEM DATE;+2026-09-16"})
    void invalidDateLeavesCurrentDateUnchanged(String command) {
        String response = processor.process(command);
        assertTrue(response.startsWith("ERROR: "), response);
        assertEquals(LocalDate.of(2026, 9, 15), processor.getCurrentDate());
    }

    @Test
    void leapDayAccepted() {
        assertTrue(processor.process("SYSTEM DATE;2028-02-29").startsWith("OK: "));
        assertEquals(LocalDate.of(2028, 2, 29), processor.getCurrentDate());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void notPersistedAcrossExecutions() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        int status = App.run(new String[0],
                new ByteArrayInputStream("SYSTEM DATE;2026-09-15\nSYSTEM DATE;bad\nEXIT\n".getBytes(StandardCharsets.UTF_8)),
                ps, ps, dir.resolve("users.txt"));
        assertEquals(0, status);
        String[] lines = out.toString(StandardCharsets.UTF_8).split("\\R");
        assertEquals("OK: System date set to 2026-09-15", lines[0]);
        assertTrue(lines[1].startsWith("ERROR: Invalid date"));
        // a new execution builds a new controller starting at the OS date again
        assertEquals(LocalDate.now(), new CommandProcessor(users).getCurrentDate());
    }
}
