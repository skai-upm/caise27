package com.parque.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.business.model.*;
import com.parque.persistence.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassServiceTest {
    static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    UserService users;
    GroupService groups;
    TicketService tickets;
    PassService passes;

    private PassService newPassService() throws Exception {
        return new PassService(new PassRepository(dir.resolve("passes.txt")), tickets);
    }

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")), new BCryptPasswordHasher(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        users.create("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "luis@correo.com");
        tickets = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, new BigDecimal("40.00"));
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, new BigDecimal("40.00"));
        passes = newPassService();
    }

    @Test
    void speedPassSingleAndPack() {
        List<Pass> one = passes.buySpeedPasses("P01", "T001", "1", OCT_1);
        assertEquals(1, one.size());
        assertEquals("P01-1", one.get(0).getId());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());
        List<Pass> ten = passes.buySpeedPasses("P02", "T001", "10", OCT_1);
        assertEquals(10, ten.size());
        assertEquals(new BigDecimal("80.00"), PassService.total(ten));
        assertEquals(10, ten.stream().map(Pass::getId).distinct().count());
        assertTrue(ten.stream().allMatch(p -> p instanceof SpeedPass && !p.isConsumed()));
    }

    @Test
    void invalidSpeedPassPurchasesCreateNothing() {
        passes.buySpeedPasses("P04", "T001", "1", OCT_1);
        passes.buySpecialPass("P05-3", "TG01-1", OCT_1);
        for (String q : List.of("2", "0", "11", "diez", "")) {
            assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P09", "T001", q, OCT_1), q);
        }
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P05", "T001", "10", OCT_1));
        assertNull(passes.find("P05-1"), "no partial pack");
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P04", "T001", "1", OCT_1));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P06", "T999", "1", OCT_1));
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P06", "TG01", "1", OCT_1),
                "group ticket ids are not individual tickets");
        assertThrows(IllegalArgumentException.class, () -> passes.buySpeedPasses("P06", "T001", "1", OCT_15.plusDays(1)),
                "past ticket");
        assertEquals(2, passes.getPasses().size());
    }

    @Test
    void specialPassOncePerTicketEvenIfConsumed() {
        Pass s = passes.buySpecialPass("S01", "T001", OCT_1);
        assertEquals(new BigDecimal("40.00"), s.getPrice());
        s.consume();
        assertThrows(IllegalArgumentException.class, () -> passes.buySpecialPass("S02", "T001", OCT_1));
        passes.buySpecialPass("S03", "TG01-2", OCT_1);
    }

    @Test
    void discountedConstituentPricing() {
        assertEquals(new BigDecimal("36.00"), tickets.find("TG01-1").getPrice());
        assertEquals(new BigDecimal("7.20"), passes.buySpeedPasses("P05", "TG01-1", "1", OCT_1).get(0).getPrice());
        assertEquals(new BigDecimal("36.00"), passes.buySpecialPass("S05", "TG01-1", OCT_1).getPrice());
        assertEquals(new BigDecimal("108.00"), tickets.findGroupTicket("TG01").getPrice(), "group total unchanged");
    }

    @Test
    void refunds() {
        passes.buySpeedPasses("P01", "T001", "10", OCT_1);
        passes.buySpecialPass("S01", "T001", OCT_1);
        assertThrows(IllegalArgumentException.class, () -> passes.refund("P01-1", SpecialPass.class, OCT_1), "kind");
        assertThrows(IllegalArgumentException.class, () -> passes.refund("P99", SpeedPass.class, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> passes.refund("P01-1", SpeedPass.class, OCT_15), "today");
        assertThrows(IllegalArgumentException.class, () -> passes.refund("P01-1", SpeedPass.class, OCT_15.plusDays(1)));
        passes.find("P01-2").consume();
        assertThrows(IllegalArgumentException.class, () -> passes.refund("P01-2", SpeedPass.class, OCT_1), "consumed");
        passes.refund("P01-1", SpeedPass.class, OCT_1);
        assertNull(passes.find("P01-1"));
        passes.refund("S01", SpecialPass.class, OCT_1);
        passes.buySpecialPass("S02", "T001", OCT_1);
    }

    @Test
    void persistenceKeepsStatusAndTicket() throws Exception {
        passes.buySpeedPasses("P02", "T001", "10", OCT_1);
        passes.buySpecialPass("S01", "T001", OCT_1);
        passes.find("S01").consume();
        passes.save();
        List<String> lines = Files.readAllLines(dir.resolve("passes.txt"));
        assertEquals("SPEED;P02-1;T001;8.00;false", lines.get(0));
        assertEquals("SPECIAL;S01;T001;40.00;true", lines.get(10));
        PassService restarted = newPassService();
        assertEquals(11, restarted.getPasses().size());
        Pass s = restarted.find("S01");
        assertTrue(s instanceof SpecialPass && s.isConsumed());
        assertSame(tickets.find("T001"), s.getTicket());
        assertThrows(IllegalArgumentException.class, () -> restarted.refund("S01", SpecialPass.class, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> restarted.buySpecialPass("S02", "T001", OCT_1));
    }

    @Test
    void removePassesOfDeletedTickets() {
        passes.buySpeedPasses("P01", "T001", "1", OCT_1);
        passes.buySpeedPasses("P02", "TG01-1", "1", OCT_1);
        passes.removePassesOf(List.of(tickets.find("TG01-1")));
        assertNull(passes.find("P02-1"));
        assertNotNull(passes.find("P01-1"));
        assertFalse(passes.passesOfUser(users.findByEmail("ana@correo.com")).isEmpty());
    }
}
