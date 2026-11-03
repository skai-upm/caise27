package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.persistence.AttractionFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemDateTest {

    static final LocalDate START = LocalDate.of(2026, 9, 15);

    @TempDir
    Path dir;
    UserController service;
    GroupController groups;
    TicketController tickets;
    AttractionController attractions;
    CommandProcessor processor;

    @BeforeEach
    void setUp() {
        service = UserController.initialize(new UserFileStorage(dir.resolve("users.txt")));
        groups = GroupController.initialize(new GroupFileStorage(dir.resolve("groups.txt")), service);
        tickets = TicketController.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                        new GroupTicketFileStorage(dir.resolve("grouptickets.txt")), service, groups);
        attractions = AttractionController.initialize(new AttractionFileStorage(dir.resolve("attractions.txt")));
        processor = new CommandProcessor(ParkController.initialize(service, groups, tickets, attractions, START));
    }

    @Test
    void defaultStartsWithOperatingSystemDate() {
        LocalDate before = LocalDate.now();
        LocalDate date = ParkController.initialize(service, groups, tickets, attractions).getCurrentDate();
        LocalDate after = LocalDate.now();
        assertTrue(!date.isBefore(before) && !date.isAfter(after));
    }

    @Test
    void injectedStartDateIsReported() {
        assertEquals(START, processor.getPark().getCurrentDate());
    }

    @Test
    void setsValidDate() {
        CommandProcessor p = new CommandProcessor(ParkController.initialize(service, groups, tickets, attractions, LocalDate.of(2000, 1, 1)));
        assertEquals("OK: System date set to 2026-09-15", p.process("SYSTEM DATE;2026-09-15"));
        assertEquals(START, p.getPark().getCurrentDate());
    }

    @Test
    void movesBackwardAndForward() {
        assertTrue(processor.process("SYSTEM DATE;2020-01-01").startsWith("OK:"));
        assertEquals(LocalDate.of(2020, 1, 1), processor.getPark().getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2030-12-31").startsWith("OK:"));
        assertEquals(LocalDate.of(2030, 12, 31), processor.getPark().getCurrentDate());
        assertTrue(processor.process("system date; 2024-02-29 ").startsWith("OK:"));
        assertEquals(LocalDate.of(2024, 2, 29), processor.getPark().getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SYSTEM DATE;15/09/2026", "SYSTEM DATE;2026-9-15", "SYSTEM DATE;hoy", "SYSTEM DATE;2026-02-30",
        "SYSTEM DATE;2025-02-29", "SYSTEM DATE;2026-13-01", "SYSTEM DATE;", "SYSTEM DATE",
        "SYSTEM DATE;2026-09-16;2026-09-17", "SYSTEM DATE;20260916"})
    void invalidDatesAreRejectedWithoutChange(String command) {
        assertTrue(processor.process(command).startsWith("ERROR:"), command);
        assertEquals(START, processor.getPark().getCurrentDate());
    }

    @Test
    void helpListsSystemDate() {
        String help = processor.process("HELP");
        for (String syntax : CommandProcessor.COMMAND_SYNTAX) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertTrue(CommandProcessor.COMMAND_SYNTAX.size() >= 6);
        assertTrue(help.contains("SYSTEM DATE;yyyy-MM-dd"));
    }
}
