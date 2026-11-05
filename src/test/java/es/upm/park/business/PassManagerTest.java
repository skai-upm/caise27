package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.Pass;
import es.upm.park.business.model.Result;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.persistence.PassFileStore;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.persistence.UserFileStore;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    @TempDir
    Path dir;
    private UserManager users;
    private TicketManager tickets;
    private PassManager passes;

    @BeforeEach
    void setUp() throws IOException {
        users = UserManager.configure(new UserFileStore(dir.resolve("users.txt")), new BCryptPasswordHasher(4));
        users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        GroupManager groups = GroupManager.configure(new GroupFileStore(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "ana@correo.com");
        tickets = TicketManager.configure(new TicketFileStore(dir.resolve("tickets.txt")),
                new GroupTicketFileStore(dir.resolve("grouptickets.txt")), users, groups);
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        tickets.create("T002", "pedro@correo.com", OCT_1, OCT_1, P40);
        tickets.createGroupTicket("TG01", groups.find("G01").orElseThrow(), OCT_15, OCT_1, P40);
        passes = reload();
    }

    private PassManager reload() throws IOException {
        return PassManager.configure(new PassFileStore(dir.resolve("passes.txt")), tickets);
    }

    @Test
    void packOfTenCreatesTenIndependentPasses() {
        Result r = passes.buySpeedPasses("SP01", "T001", "10", OCT_1);
        assertEquals("OK: SpeedPasses SP01-1..SP01-10 purchased for ticket T001. Total: 80.00", r.toString());
        assertEquals(10, passes.getPasses().size());
        for (int i = 1; i <= 10; i++) {
            Pass p = passes.find("SP01-" + i).orElseThrow();
            assertEquals(new BigDecimal("8.00"), p.getPrice());
            assertFalse(p.isConsumed());
        }
        passes.find("SP01-1").orElseThrow().consume();
        assertFalse(passes.find("SP01-2").orElseThrow().isConsumed(), "independent state");
    }

    @Test
    void singleSpeedPass() {
        assertEquals("OK: SpeedPasses SP02-1 purchased for ticket T001. Total: 8.00",
                passes.buySpeedPasses("SP02", "T001", "1", OCT_1).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "2", "5", "9", "11", "-1", "uno", "01"})
    void disallowedQuantitiesCreateNothing(String quantity) {
        assertFalse(passes.buySpeedPasses("SP03", "T001", quantity, OCT_1).success());
        assertTrue(passes.getPasses().isEmpty());
    }

    @Test
    void identifierConflictCreatesNothing() throws IOException {
        passes.buySpecialPass("SP04-7", "T001", OCT_1);
        List<String> before = Files.readAllLines(dir.resolve("passes.txt"));
        assertFalse(passes.buySpeedPasses("SP04", "T001", "10", OCT_1).success());
        assertEquals(1, passes.getPasses().size());
        assertEquals(before, Files.readAllLines(dir.resolve("passes.txt")));
    }

    @Test
    void constituentTicketUsesItsStoredPrice() {
        assertTrue(passes.buySpeedPasses("SP05", "TG01-12345678Z", "1", OCT_1).success());
        assertEquals(new BigDecimal("8.00"), passes.find("SP05-1").orElseThrow().getPrice());
        assertTrue(passes.buySpecialPass("SPC9", "TG01-87654321X", OCT_1).toString().endsWith("Total: 40.00"));
    }

    @Test
    void unknownOrPastTicketRejected() {
        assertFalse(passes.buySpeedPasses("SP06", "T999", "1", OCT_1).success());
        assertFalse(passes.buySpecialPass("SPC6", "TG01", OCT_1).success(), "group ticket is not an individual ticket");
        assertFalse(passes.buySpeedPasses("SP06", "T002", "1", OCT_1.plusDays(1)).success());
        assertTrue(passes.buySpeedPasses("SP06", "T002", "1", OCT_1).success(), "ticket for today allowed");
    }

    @Test
    void onlyOneSpecialPassPerTicketEvenIfConsumed() {
        assertEquals("OK: Special Pass SPC1 purchased for ticket T001. Total: 40.00",
                passes.buySpecialPass("SPC1", "T001", OCT_1).toString());
        assertFalse(passes.buySpecialPass("SPC2", "T001", OCT_1).success());
        passes.find("SPC1").orElseThrow().consume();
        assertFalse(passes.buySpecialPass("SPC3", "T001", OCT_1).success());
        assertFalse(passes.buySpecialPass("SPC1", "TG01-12345678Z", OCT_1).success(), "id taken");
    }

    @Test
    void refundRules() {
        passes.buySpeedPasses("SP07", "T001", "10", OCT_1);
        passes.buySpecialPass("SPC7", "T001", OCT_1);
        assertEquals("OK: Pass SP07-3 refunded: 8.00", passes.refund("SP07-3", SpeedPass.TYPE_KEY, OCT_1).toString());
        assertTrue(passes.find("SP07-3").isEmpty());
        assertFalse(passes.refund("SP07-4", SpeedPass.TYPE_KEY, OCT_15).success(), "ticket dated today");
        assertFalse(passes.refund("SP07-4", SpeedPass.TYPE_KEY, OCT_15.plusDays(1)).success(), "past ticket");
        passes.find("SP07-5").orElseThrow().consume();
        assertFalse(passes.refund("SP07-5", SpeedPass.TYPE_KEY, OCT_1).success(), "consumed");
        assertFalse(passes.refund("SPC7", SpeedPass.TYPE_KEY, OCT_1).success(), "wrong kind");
        assertFalse(passes.refund("NOPE", SpeedPass.TYPE_KEY, OCT_1).success());
        assertTrue(passes.refund("SPC7", "SPECIAL", OCT_1).success());
        assertTrue(passes.buySpecialPass("SPC8", "T001", OCT_1).success(), "refunded special pass may be bought again");
    }

    @Test
    void passesSurviveRestartWithConsumedStatus() throws IOException {
        passes.buySpeedPasses("SP08", "T001", "10", OCT_1);
        passes.buySpecialPass("SPC1", "TG01-12345678Z", OCT_1);
        passes.find("SP08-2").orElseThrow().consume();
        passes.refund("SP08-10", SpeedPass.TYPE_KEY, OCT_1);
        PassManager again = reload();
        assertEquals(10, again.getPasses().size());
        assertTrue(again.find("SP08-2").orElseThrow().isConsumed());
        assertEquals("TG01-12345678Z", again.find("SPC1").orElseThrow().getTicket().getId());
        assertEquals(new BigDecimal("40.00"), again.find("SPC1").orElseThrow().getPrice());
    }

    @Test
    void passesOfUserIncludeConstituentTickets() {
        passes.buySpeedPasses("SP09", "TG01-12345678Z", "1", OCT_1);
        passes.buySpeedPasses("SP10", "TG01-87654321X", "1", OCT_1);
        User ana = users.findByEmail("ana@correo.com").orElseThrow();
        assertEquals(List.of("SP09-1"), passes.passesOf(ana).stream().map(Pass::getId).toList());
    }
}
