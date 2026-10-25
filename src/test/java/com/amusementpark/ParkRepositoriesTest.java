package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ParkRepositoriesTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private final User ana = TicketTest.user(1);
    private final User pedro = TicketTest.user(2);
    private final List<User> users = List.of(ana, pedro);

    @Test
    public void groupsRoundTripLinkedToUsers() throws Exception {
        GroupRepository repository = new GroupRepository(dir.resolve("groups.txt"));
        Group group = new Group("G01", "Familia Garcia");
        group.add(ana);
        group.add(pedro);
        repository.save(List.of(group, new Group("G02", "Vacio")));
        assertEquals(List.of("G01;Familia Garcia;dni1,dni2", "G02;Vacio;"), Files.readAllLines(dir.resolve("groups.txt")));

        List<Group> loaded = repository.load(users);
        assertEquals(2, loaded.size());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    public void malformedGroupLinesSkipped() throws Exception {
        Files.write(dir.resolve("groups.txt"), List.of("bad", "G01;Familia;dni1"));
        List<Group> loaded = new GroupRepository(dir.resolve("groups.txt")).load(users);
        assertEquals(1, loaded.size());
    }

    @Test
    public void ticketsRoundTripWithDetachedHistory() throws Exception {
        TicketRepository repository = new TicketRepository(dir.resolve("tickets.txt"));
        User gone = TicketTest.user(9);
        repository.save(List.of(new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana),
                new IndividualTicket("T002", DAY, new BigDecimal("60.00"), gone)));
        assertEquals(List.of("T001;2026-10-15;40.00;dni1", "T002;2026-10-15;60.00;dni9"),
                Files.readAllLines(dir.resolve("tickets.txt")));

        List<IndividualTicket> loaded = repository.load(users);
        assertSame(ana, loaded.get(0).getUser());
        assertEquals(DAY, loaded.get(0).getDate());
        assertEquals(new BigDecimal("40.00"), loaded.get(0).getPrice());
        assertEquals("dni9", loaded.get(1).getUser().getDni());
        assertNotSame(gone, loaded.get(1).getUser());
    }

    @Test
    public void groupTicketsRoundTrip() throws Exception {
        GroupTicketRepository repository = new GroupTicketRepository(dir.resolve("group_tickets.txt"));
        Group group = new Group("G01", "Familia");
        group.add(ana);
        group.add(pedro);
        repository.save(List.of(GroupTicket.purchase("TG01", DAY, new BigDecimal("40.00"), group)));
        assertEquals(List.of("TG01;2026-10-15;80.00;G01;Familia;TG01-1:dni1:40.00,TG01-2:dni2:40.00"),
                Files.readAllLines(dir.resolve("group_tickets.txt")));

        GroupTicket loaded = repository.load(users, List.of(group)).get(0);
        assertSame(group, loaded.getGroup());
        assertEquals(List.of(ana, pedro), loaded.getUsers());
        assertEquals(new BigDecimal("80.00"), loaded.getPrice());

        GroupTicket detached = repository.load(users, List.of()).get(0);
        assertEquals("G01", detached.getGroup().getId());
        assertEquals("Familia", detached.getGroup().getName());
    }

    @Test
    public void attractionsRoundTripEmpty() throws Exception {
        AttractionRepository repository = new AttractionRepository(dir.resolve("attractions.txt"));
        Attraction attraction = new Attraction("A01", "Tornado", 20);
        attraction.enter(new IndividualTicket("T", DAY, BigDecimal.TEN, ana));
        repository.save(List.of(attraction));
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        Attraction loaded = repository.load().get(0);
        assertEquals("Tornado", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.getPresent().isEmpty());
    }
}
