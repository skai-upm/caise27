package es.upm.etsisi.amusementpark.business.controller;

import static es.upm.etsisi.amusementpark.business.controller.ParkTest.TODAY;
import static es.upm.etsisi.amusementpark.business.controller.ParkTest.TOMORROW;
import static es.upm.etsisi.amusementpark.business.controller.ParkTest.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import es.upm.etsisi.amusementpark.business.repository.Repositories;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.user.Passwords;
import es.upm.etsisi.amusementpark.business.user.User;
import es.upm.etsisi.amusementpark.business.user.UserField;
import es.upm.etsisi.amusementpark.persistence.FileRepositories;

class Delivery3Test {

    @TempDir
    Path dir;

    ParkController park;
    InvoiceController invoices;

    @BeforeEach
    void setUp() throws IOException {
        park = ParkController.getInstance();
        park.configure(FileRepositories.in(dir));
        park.setCurrentDate(TODAY);
        invoices = InvoiceController.getInstance();
        invoices.configure(park);
    }

    private void restart() throws IOException {
        park.configure(FileRepositories.in(dir));
        park.setCurrentDate(TODAY);
    }

    /** Users 1..n in group G01 with a group ticket TG01 for the given date. */
    private GroupTicket groupTicket(int members, java.time.LocalDate date) throws ParkException {
        park.createGroup("G01", "Familia");
        for (int i = 1; i <= members; i++) {
            if (park.getUsers().findByEmail(user(i).getEmail()) == null) {
                park.createUser(user(i));
            }
            park.addToGroup("G01", user(i).getEmail());
        }
        return park.createGroupTicket("TG01", "G01", date);
    }

    private IndividualTicket ticketForUser1(String id, java.time.LocalDate date) throws ParkException {
        if (park.getUsers().findByEmail(user(1).getEmail()) == null) {
            park.createUser(user(1));
        }
        return park.createTicket(id, user(1).getEmail(), date);
    }

    // ---- F3-C03 singletons and injection ----------------------------------------------------

    @Test
    void controllersAreSingletons() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void injectedRepositoryImplementationIsUsed() throws Exception {
        Repositories files = FileRepositories.in(dir);
        List<List<Pass>> saved = new ArrayList<>();
        PassRepository inMemory = new PassRepository() {
            @Override
            public List<Pass> load(Map<String, IndividualTicket> ticketsById) {
                return List.of();
            }

            @Override
            public void save(List<Pass> passes) {
                saved.add(List.copyOf(passes));
            }

            @Override
            public List<String> getWarnings() {
                return List.of();
            }
        };
        park.configure(new Repositories(files.users(), files.groups(), files.tickets(), files.attractions(),
                inMemory));
        park.setCurrentDate(TODAY);
        ticketForUser1("T001", TODAY);
        park.createSpeedPasses("SP01", "T001", 1);
        assertEquals(1, saved.size());
        assertFalse(Files.exists(dir.resolve("passes.txt")));
    }

    @Test
    void failingRepositoryRaisesStorageExceptionWithoutChanges() throws Exception {
        Repositories files = FileRepositories.in(dir);
        PassRepository failing = new PassRepository() {
            @Override
            public List<Pass> load(Map<String, IndividualTicket> ticketsById) {
                return List.of();
            }

            @Override
            public void save(List<Pass> passes) throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public List<String> getWarnings() {
                return List.of();
            }
        };
        park.configure(new Repositories(files.users(), files.groups(), files.tickets(), files.attractions(),
                failing));
        park.setCurrentDate(TODAY);
        ticketForUser1("T001", TODAY);
        assertThrows(StorageException.class, () -> park.createSpeedPasses("SP01", "T001", 10));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void exceptionTypesDescribeTheSituation() throws Exception {
        assertThrows(NotFoundException.class, () -> park.createTicket("T001", "nobody@correo.com", TODAY));
        assertThrows(InvalidDataException.class, () -> park.setBasePrice(BigDecimal.ZERO));
        ticketForUser1("T001", TODAY);
        assertThrows(RuleViolationException.class, () -> park.createTicket("T001", user(1).getEmail(), TODAY));
    }

    // ---- F3-C05 BCrypt ------------------------------------------------------------------------

    @Test
    void passwordsAreStoredAsBcryptHashes() throws Exception {
        park.createUser(user(1));
        String stored = Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        assertFalse(stored.contains("Clave1!"));
        String hash = stored.trim().split(";")[4];
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(Passwords.matches("Clave1!", hash));
        assertThrows(ParkException.class, () -> park.createUser(new User("X", "Y", "87654321X", "x@correo.com",
                "clave1!", "6", "1234567890123456")));
    }

    @Test
    void onlyTheNewPasswordAuthenticatesAfterChange() throws Exception {
        park.createUser(user(1));
        park.updateUser(user(1).getEmail(), "Clave1!", UserField.PASSWORD, "Nueva2#");
        restart();
        assertThrows(ParkException.class, () -> park.getUsers().authenticate(user(1).getEmail(), "Clave1!"));
        assertNotNull(park.getUsers().authenticate(user(1).getEmail(), "Nueva2#"));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
    }

    // ---- F3-C06 passes ------------------------------------------------------------------------

    @Test
    void passPricesComeFromTheTicket() throws Exception {
        ticketForUser1("T001", TODAY);
        park.setBasePrice(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("8.00"), park.createSpeedPasses("SP01", "T001", 1).get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), park.createSpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void discountedCompositionTicketPricesPasses() throws Exception {
        GroupTicket gt = groupTicket(3, TODAY);
        String partId = gt.getComposition().get(0).getIdentifier();
        assertEquals(new BigDecimal("36.00"), gt.getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("7.20"), park.createSpeedPasses("SP01", partId, 1).get(0).getPrice());
    }

    @Test
    void speedPassPackOfTen() throws Exception {
        ticketForUser1("T001", TODAY);
        List<SpeedPass> pack = park.createSpeedPasses("SP02", "T001", 10);
        assertEquals(10, pack.size());
        assertEquals("SP02:1", pack.get(0).getIdentifier());
        assertEquals("SP02:10", pack.get(9).getIdentifier());
        assertEquals(new BigDecimal("80.00"), pack.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO,
                BigDecimal::add));
        assertEquals("SP01:1", park.createSpeedPasses("SP01", "T001", 1).get(0).getIdentifier());
    }

    @Test
    void speedPassRejections() throws Exception {
        ticketForUser1("T001", TODAY);
        groupTicket(1, TODAY);
        park.createSpeedPasses("SP02", "T001", 1);
        assertThrows(InvalidDataException.class, () -> park.createSpeedPasses("SP03", "T001", 5));
        assertThrows(NotFoundException.class, () -> park.createSpeedPasses("SP03", "T999", 1));
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP03", "TG01", 1));
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP02", "T001", 10));
        park.setCurrentDate(TOMORROW);
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP04", "T001", 1));
        assertEquals(1, park.getPasses().size());
    }

    @Test
    void oneSpecialPassPerTicket() throws Exception {
        ticketForUser1("T001", TODAY);
        park.createSpecialPass("PE01", "T001");
        assertThrows(RuleViolationException.class, () -> park.createSpecialPass("PE02", "T001"));
        park.createAttraction("A04", "Terror", 5, AttractionType.SPECIAL);
        park.enter("A04", "T001", false);
        assertThrows(RuleViolationException.class, () -> park.createSpecialPass("PE02", "T001"));
        assertTrue(park.findPass("PE01").isConsumed());
    }

    @Test
    void refunds() throws Exception {
        ticketForUser1("T009", TOMORROW);
        park.createSpeedPasses("SP02", "T009", 10);
        park.createSpecialPass("PE01", "T009");
        park.deleteSpeedPass("SP02:3");
        assertNull(park.findPass("SP02:3"));
        assertNotNull(park.findPass("SP02:1"));
        park.deleteSpecialPass("PE01");
        assertNull(park.findPass("PE01"));
        assertThrows(NotFoundException.class, () -> park.deleteSpecialPass("SP02:1"));
        assertThrows(NotFoundException.class, () -> park.deleteSpeedPass("SP02:3"));
    }

    @Test
    void refundRejectedForTodayOrConsumed() throws Exception {
        ticketForUser1("T001", TODAY);
        park.createSpeedPasses("SP01", "T001", 1);
        assertThrows(RuleViolationException.class, () -> park.deleteSpeedPass("SP01:1"));
        ticketForUser1("T009", TOMORROW);
        park.createSpeedPasses("SP09", "T009", 1);
        park.findPass("SP09:1").setConsumed(true);
        assertThrows(RuleViolationException.class, () -> park.deleteSpeedPass("SP09:1"));
        assertEquals(2, park.getPasses().size());
    }

    @Test
    void ticketDeletionCascadesToPasses() throws Exception {
        ticketForUser1("T009", TOMORROW);
        park.createSpeedPasses("SP01", "T009", 1);
        park.createSpeedPasses("SP02", "T009", 10);
        park.createSpecialPass("PE01", "T009");
        park.deleteTicket("T009");
        assertTrue(park.getPasses().isEmpty());
        restart();
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void groupTicketDeletionCascades() throws Exception {
        GroupTicket gt = groupTicket(2, TOMORROW);
        for (IndividualTicket part : gt.getComposition()) {
            park.createSpeedPasses("SP-" + part.getUser().getDni(), part.getIdentifier(), 1);
        }
        park.createSpecialPass("PE01", gt.getComposition().get(0).getIdentifier());
        park.deleteGroupTicket("TG01");
        assertTrue(park.getPasses().isEmpty());
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket(gt.getComposition().get(0).getIdentifier()));
    }

    // ---- F3-C07 / C08 typed attractions and pass consumption ------------------------------------

    @Test
    void typesRestrictAcceptedTickets() throws Exception {
        ticketForUser1("T001", TODAY);
        groupTicket(1, TODAY);
        park.createAttraction("A01", "General", 20, AttractionType.GENERAL);
        park.createAttraction("A02", "Individual", 20, AttractionType.INDIVIDUAL);
        park.createAttraction("A03", "Group", 20, AttractionType.GROUP);
        park.createAttraction("A04", "Special", 20, AttractionType.SPECIAL);
        assertThrows(RuleViolationException.class, () -> park.enter("A02", "TG01", false));
        assertThrows(RuleViolationException.class, () -> park.enter("A03", "T001", false));
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "TG01", false));
        assertEquals(1, park.enter("A02", "T001", false).users().size());
        assertEquals(1, park.enter("A03", "TG01", false).users().size());
        park.exit("A02", user(1).getEmail());
        park.exit("A03", user(1).getEmail());
        assertEquals(1, park.enter("A01", "T001", false).users().size());
        park.exit("A01", user(1).getEmail());
        assertEquals(1, park.enter("A01", "TG01", false).users().size());
        assertTrue(park.requireAttraction("A03").accepts(park.findTicket("TG01")));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() throws Exception {
        ticketForUser1("T001", TODAY);
        park.createAttraction("A01", "General", 20, AttractionType.GENERAL);
        assertThrows(RuleViolationException.class, () -> park.enter("A01", "T001", true));
        park.createSpeedPasses("SP02", "T001", 10);
        EntryResult entry = park.enter("A01", "T001", true);
        assertEquals(1, entry.consumedPasses().size());
        assertEquals(1, park.getPasses().stream().filter(Pass::isConsumed).count());
    }

    @Test
    void groupSpeedEntryIsAtomic() throws Exception {
        GroupTicket gt = groupTicket(3, TODAY);
        park.createAttraction("A01", "General", 20, AttractionType.GENERAL);
        park.createSpeedPasses("SPA", gt.getComposition().get(0).getIdentifier(), 1);
        park.createSpeedPasses("SPB", gt.getComposition().get(1).getIdentifier(), 1);
        assertThrows(RuleViolationException.class, () -> park.enter("A01", "TG01", true));
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertTrue(park.getPasses().stream().noneMatch(Pass::isConsumed));

        park.createSpeedPasses("SPC", gt.getComposition().get(2).getIdentifier(), 10);
        EntryResult entry = park.enter("A01", "TG01", true);
        assertEquals(3, entry.users().size());
        assertEquals(3, entry.consumedPasses().size());
        assertEquals(3, park.getPasses().stream().filter(Pass::isConsumed).count());
    }

    @Test
    void groupSpeedEntryRespectsCapacityWithoutConsuming() throws Exception {
        GroupTicket gt = groupTicket(2, TODAY);
        park.createAttraction("A03", "Group", 1, AttractionType.GROUP);
        for (IndividualTicket part : gt.getComposition()) {
            park.createSpeedPasses("SP-" + part.getUser().getDni(), part.getIdentifier(), 1);
        }
        assertThrows(RuleViolationException.class, () -> park.enter("A03", "TG01", true));
        assertTrue(park.getPasses().stream().noneMatch(Pass::isConsumed));
    }

    @Test
    void specialAttractionConsumesSpecialPassAutomatically() throws Exception {
        ticketForUser1("T001", TODAY);
        park.createAttraction("A04", "Terror", 5, AttractionType.SPECIAL);
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", false));
        SpecialPass pass = park.createSpecialPass("PE01", "T001");
        park.createSpeedPasses("SP01", "T001", 1);
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", true));
        assertFalse(pass.isConsumed());
        EntryResult entry = park.enter("A04", "T001", false);
        assertEquals(List.of(pass), entry.consumedPasses());
        assertTrue(pass.isConsumed());
        park.exit("A04", user(1).getEmail());
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", false));
        assertNotNull(park.findPass("PE01"));
    }

    @Test
    void failedSpecialEntryLeavesPassUntouched() throws Exception {
        ticketForUser1("T001", TOMORROW);
        park.createAttraction("A04", "Terror", 5, AttractionType.SPECIAL);
        SpecialPass pass = park.createSpecialPass("PE01", "T001");
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", false));
        assertFalse(pass.isConsumed());
    }

    // ---- F3-C09 persistence ---------------------------------------------------------------------

    @Test
    void passesAndTypesSurviveRestart() throws Exception {
        GroupTicket gt = groupTicket(1, TODAY);
        ticketForUser1("T001", TODAY);
        park.createAttraction("A01", "General", 20, AttractionType.GENERAL);
        park.createAttraction("A04", "Terror", 5, AttractionType.SPECIAL);
        park.createSpeedPasses("SP01", "T001", 1);
        park.createSpecialPass("PE01", gt.getComposition().get(0).getIdentifier());
        park.enter("A01", "T001", true);

        restart();

        Pass speed = park.findPass("SP01:1");
        assertTrue(speed.isConsumed());
        assertSame(park.findTicket("T001"), speed.getTicket());
        assertEquals(new BigDecimal("8.00"), speed.getPrice());
        Pass special = park.findPass("PE01");
        assertFalse(special.isConsumed());
        assertSame(park.findTicket(gt.getComposition().get(0).getIdentifier()), special.getTicket());
        assertEquals(AttractionType.SPECIAL, park.requireAttraction("A04").getType());
        assertTrue(park.requireAttraction("A01").getOccupants().isEmpty());
        assertTrue(park.getLoadWarnings().isEmpty());
    }

    @Test
    void legacyAttractionLinesAreGeneral() throws Exception {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20", "A02;Bad;5;VIP"),
                StandardCharsets.UTF_8);
        restart();
        Attraction<?> a = park.requireAttraction("A01");
        assertEquals(AttractionType.GENERAL, a.getType());
        assertNull(park.findAttraction("A02"));
        assertEquals(1, park.getLoadWarnings().size());
    }

    // ---- F3-C02 invoices --------------------------------------------------------------------------

    @Test
    void userInvoiceCountsDirectTicketsAndPersonalPasses() throws Exception {
        GroupTicket gt = groupTicket(3, TODAY);
        ticketForUser1("T001", TODAY);
        park.createSpeedPasses("SP01", gt.getComposition().get(0).getIdentifier(), 1);
        Invoice invoice = invoices.userInvoice(user(1).getEmail());
        assertEquals(new BigDecimal("47.20"), invoice.getTotal());
        assertEquals(List.of("Individual tickets", "SpeedPasses"),
                invoice.getSections().stream().map(s -> s.title()).toList());
        String text = invoice.render();
        assertTrue(text.contains("T001"));
        assertFalse(text.contains("Ticket TG01:"), "composition tickets are not individual purchases");
        assertTrue(text.contains(user(1).getDni()));
    }

    @Test
    void userInvoiceOmitsEmptySections() throws Exception {
        ticketForUser1("T001", TODAY);
        Invoice invoice = invoices.userInvoice(user(1).getEmail());
        assertEquals(1, invoice.getSections().size());
        assertEquals(new BigDecimal("40.00"), invoice.getTotal());
        assertThrows(NotFoundException.class, () -> invoices.userInvoice("nobody@correo.com"));
    }

    @Test
    void groupInvoiceCountsGroupTicketOnce() throws Exception {
        GroupTicket gt = groupTicket(3, TODAY);
        park.createSpeedPasses("SP01", gt.getComposition().get(0).getIdentifier(), 10);
        Invoice invoice = invoices.groupInvoice("G01");
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertEquals(1, invoice.getSections().size());
        assertEquals(3, invoice.getSections().get(0).lines().get(0).details().size());
        assertTrue(invoice.render().contains("G01 - Familia"));
        assertThrows(NotFoundException.class, () -> invoices.groupInvoice("G99"));
    }

    @Test
    void groupWithoutTicketsHasEmptyInvoice() throws Exception {
        park.createGroup("G02", "Vacio");
        Invoice invoice = invoices.groupInvoice("G02");
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }
}
