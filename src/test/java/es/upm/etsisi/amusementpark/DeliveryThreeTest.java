package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryThreeTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() throws ParkException {
        park = TestSupport.initialize(dir);
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Luis", "Perez", "00000000T", "luis@correo.com", "Clave1!", "600000000",
                "1111222233334444");
        park.createTicket("T001", "ana@correo.com", "2027-04-15");
        park.createTicket("T002", "pedro@correo.com", "2027-04-20");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "luis@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
    }

    private ParkController restart() throws ParkException {
        return TestSupport.initialize(dir);
    }

    private Pass pass(String identifier) {
        return park.getPasses().stream().filter(p -> p.getIdentifier().equals(identifier)).findFirst().orElseThrow();
    }

    // ------------------------------------------------------- C03 singletons, DI

    @Test
    void controllersAreSingletons() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void controllerWorksWithAnotherRepositoryImplementation() throws ParkException {
        InMemoryRepositories memory = new InMemoryRepositories();
        ParkController controller = ParkController.getInstance();
        controller.initialize(memory.bundle(), TestSupport.HASHER, TestSupport.TODAY);
        controller.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        controller.createAttraction("A01", "Dragon", "5", "SPECIAL");
        assertEquals(1, memory.users.size());
        assertEquals(AttractionType.SPECIAL, memory.attractions.get(0).getType());
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    // ------------------------------------------------------------- C05 BCrypt

    @Test
    void passwordsAreStoredAsBcryptHashes() throws Exception {
        String users = Files.readString(dir.resolve("users.txt"));
        assertFalse(users.contains("Clave1!"));
        assertFalse(users.contains("Otra2#"));
        String hash = park.getUsers().get(0).getPasswordHash();
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(TestSupport.HASHER.matches("Clave1!", hash));
        assertThrows(InvalidDataException.class, () -> park.createUser("X", "Y", "11111111H", "x@correo.com",
                "clave1!", "1", "1234567890123456"));
    }

    @Test
    void onlyTheNewPasswordAuthenticatesAfterAChange() throws ParkException {
        park.updateUser("luis@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        ParkController reloaded = restart();
        assertThrows(RuleViolationException.class, () -> reloaded.updateUser("luis@correo.com", "Clave1!", "PHONE", "1"));
        reloaded.updateUser("luis@correo.com", "Nueva2#", "PHONE", "2");
        assertEquals("2", reloaded.requireUser("luis@correo.com").getPhone());
    }

    @Test
    void legacyPlaintextPasswordFailsCleanly() throws Exception {
        Files.write(dir.resolve("users.txt"),
                List.of("Old;User;11111111H;old@correo.com;Clave1!;1;1234567890123456"));
        Files.delete(dir.resolve("tickets.txt"));
        ParkController reloaded = restart();
        assertThrows(RuleViolationException.class, () -> reloaded.deleteUser("old@correo.com", "Clave1!"));
        assertEquals(1, reloaded.getUsers().size());
    }

    // -------------------------------------------------------------- C06 passes

    @Test
    void speedPassPurchases() throws ParkException {
        List<SpeedPass> single = park.createSpeedPasses("SP01", "T001", "1");
        assertEquals("SP01:1", single.get(0).getIdentifier());
        assertEquals(new BigDecimal("8.00"), single.get(0).getPrice());

        List<SpeedPass> pack = park.createSpeedPasses("SP02", "T001", "10");
        assertEquals(10, pack.size());
        assertEquals("SP02:10", pack.get(9).getIdentifier());
        assertEquals(new BigDecimal("80.00"), pack.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        // composed ticket of a 3-member group: 40 - 10 % = 36.00, pass = 7.20
        assertEquals(new BigDecimal("7.20"), park.createSpeedPasses("SP03", "TG01:12345678Z", "1").get(0).getPrice());

        park.setPrice("50");
        assertEquals(new BigDecimal("8.00"), park.createSpeedPasses("SP04", "T001", "1").get(0).getPrice());
    }

    @Test
    void speedPassRejections() throws ParkException {
        park.createSpeedPasses("SP01", "T001", "1");
        int before = park.getPasses().size();
        assertThrows(InvalidDataException.class, () -> park.createSpeedPasses("SP09", "T001", "2"));
        assertThrows(NotFoundException.class, () -> park.createSpeedPasses("SP09", "T999", "1"));
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP09", "TG01", "1"));
        park.createSpecialPass("SP02:7", "T002");
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP02", "T001", "10"));
        assertEquals(before + 1, park.getPasses().size());
        park.setDate("2027-04-16");
        assertThrows(RuleViolationException.class, () -> park.createSpeedPasses("SP09", "T001", "1"));
    }

    @Test
    void specialPassIsUniquePerTicket() throws ParkException {
        SpecialPass pass = park.createSpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        assertThrows(RuleViolationException.class, () -> park.createSpecialPass("PE02", "T001"));
        assertThrows(RuleViolationException.class, () -> park.createSpecialPass("PE01", "T002"));
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.enter("A04", "T001", null);
        assertTrue(pass.isConsumed());
        assertThrows(RuleViolationException.class, () -> park.createSpecialPass("PE03", "T001"));
    }

    @Test
    void refundRules() throws ParkException {
        park.createSpeedPasses("SP01", "T002", "10");
        park.createSpeedPasses("SP02", "T001", "1");
        park.createSpecialPass("PE01", "T002");
        park.deletePass("SP01:3", SpeedPass.class);
        assertEquals(11, park.getPasses().size());
        assertThrows(NotFoundException.class, () -> park.deletePass("SP01:3", SpeedPass.class));
        assertThrows(NotFoundException.class, () -> park.deletePass("PE01", SpeedPass.class));
        assertThrows(RuleViolationException.class, () -> park.deletePass("SP02:1", SpeedPass.class));
        park.deletePass("PE01", SpecialPass.class);
        park.createSpecialPass("PE02", "T002");

        park.setDate("2027-04-20");
        park.createAttraction("A01", "Dragon", "10", null);
        park.enter("A01", "T002", "SPEED");
        park.setDate("2027-04-15");
        Pass consumed = park.getPasses().stream().filter(Pass::isConsumed).findFirst().orElseThrow();
        assertThrows(RuleViolationException.class, () -> park.deletePass(consumed.getIdentifier(), SpeedPass.class));
    }

    @Test
    void ticketDeletionCascadesToPasses() throws ParkException {
        park.createSpeedPasses("SP01", "T002", "1");
        park.createSpeedPasses("SP02", "T002", "10");
        park.createSpecialPass("PE01", "T002");
        park.createSpeedPasses("SP03", "T001", "1");
        park.deleteTicket("T002");
        assertEquals(List.of("SP03:1"), park.getPasses().stream().map(Pass::getIdentifier).toList());

        park.createGroupTicket("TG02", "G01", "2027-04-25");
        park.createSpeedPasses("SP04", "TG02:12345678Z", "1");
        park.createSpecialPass("PE02", "TG02:87654321X");
        park.deleteGroupTicket("TG02");
        assertEquals(1, park.getPasses().size());
        assertEquals(null, park.findTicket("TG02:12345678Z"));
        assertEquals(1, restart().getPasses().size());
    }

    // ---------------------------------------------------------- C07 types

    @Test
    void attractionTypesRestrictTicketKinds() throws ParkException {
        park.createAttraction("A01", "Dragon Rojo", "20", null);
        park.createAttraction("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        park.createAttraction("A03", "Rafting Familiar", "24", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        assertEquals(AttractionType.GENERAL, park.requireAttraction("A01").getType());
        assertThrows(InvalidDataException.class, () -> park.createAttraction("A05", "X", "5", "VIP"));

        assertThrows(RuleViolationException.class, () -> park.enter("A02", "TG01", null));
        assertThrows(RuleViolationException.class, () -> park.enter("A03", "T001", null));
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "TG01", null));
        assertTrue(park.requireAttraction("A02").getOccupants().isEmpty());

        assertEquals(1, park.enter("A02", "T001", null).admitted().size());
        assertEquals(3, park.enter("A03", "TG01", null).admitted().size());
        assertEquals(3, park.enter("A01", "TG01", null).admitted().size());
    }

    // ---------------------------------------------------------- C08 consumption

    @Test
    void individualSpeedEntryConsumesOnePass() throws ParkException {
        park.createAttraction("A01", "Dragon", "20", null);
        park.createSpeedPasses("SP01", "T001", "10");
        EntryResult result = park.enter("A01", "T001", "SPEED");
        assertEquals(1, result.consumedPasses().size());
        assertTrue(result.consumedPasses().get(0).isConsumed());
        assertEquals(1, park.getPasses().stream().filter(Pass::isConsumed).count());
        assertThrows(InvalidDataException.class, () -> park.enter("A01", "T001", "FAST"));
        assertThrows(RuleViolationException.class, () -> park.enter("A01", "TG01:87654321X", "SPEED"));
    }

    @Test
    void groupSpeedEntryIsAtomic() throws ParkException {
        park.createAttraction("A03", "Rafting", "24", "GROUP");
        park.createSpeedPasses("SA", "TG01:12345678Z", "1");
        park.createSpeedPasses("SB", "TG01:87654321X", "1");
        assertThrows(RuleViolationException.class, () -> park.enter("A03", "TG01", "SPEED"));
        assertTrue(park.requireAttraction("A03").getOccupants().isEmpty());
        assertTrue(park.getPasses().stream().noneMatch(Pass::isConsumed));

        park.createSpeedPasses("SC", "TG01:00000000T", "1");
        EntryResult result = park.enter("A03", "TG01", "SPEED");
        assertEquals(3, result.admitted().size());
        assertEquals(3, result.consumedPasses().size());
        assertTrue(restart().getPasses().stream().allMatch(Pass::isConsumed));
    }

    @Test
    void fullAttractionLeavesSpeedPassUnused() throws ParkException {
        park.createAttraction("A01", "Mini", "1", null);
        park.createSpeedPasses("SP01", "T001", "1");
        park.enter("A01", "TG01:87654321X", null);
        assertThrows(RuleViolationException.class, () -> park.enter("A01", "T001", "SPEED"));
        assertFalse(pass("SP01:1").isConsumed());
    }

    @Test
    void specialAttractionEntry() throws ParkException {
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", null));
        park.createSpecialPass("PE01", "T001");
        park.createSpeedPasses("SP01", "T001", "1");
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", "SPEED"));
        assertFalse(pass("PE01").isConsumed());
        assertFalse(pass("SP01:1").isConsumed());

        EntryResult result = park.enter("A04", "T001", null);
        assertSame(pass("PE01"), result.consumedPasses().get(0));
        assertTrue(pass("PE01").isConsumed());
        park.exit("A04", "ana@correo.com");
        assertThrows(RuleViolationException.class, () -> park.enter("A04", "T001", null));
        assertTrue(restart().getPasses().stream().filter(p -> p instanceof SpecialPass).allMatch(Pass::isConsumed));
    }

    // ---------------------------------------------------------- C02 invoices

    @Test
    void userInvoiceCountsDirectTicketsAndPersonalPasses() throws ParkException {
        park.createSpeedPasses("SP01", "TG01:12345678Z", "1");
        park.createSpecialPass("PE01", "T001");
        Invoice invoice = InvoiceController.getInstance().create("USER", "ana@correo.com");
        assertEquals("INVOICE - USER", invoice.getTitle());
        assertTrue(invoice.getHeader().contains("DNI: 12345678Z"));
        assertEquals(List.of("Individual tickets", "SpeedPasses", "Special Passes"),
                invoice.getSections().stream().map(Invoice.Section::title).toList());
        assertEquals(1, invoice.getSections().get(0).items().size());
        assertEquals(new BigDecimal("87.20"), invoice.getTotal());

        Invoice luis = InvoiceController.getInstance().userInvoice("luis@correo.com");
        assertTrue(luis.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), luis.getTotal());
        assertThrows(NotFoundException.class, () -> InvoiceController.getInstance().userInvoice("x@correo.com"));
    }

    @Test
    void groupInvoiceCountsGroupTicketsOnce() throws ParkException {
        park.createSpeedPasses("SP01", "TG01:12345678Z", "10");
        Invoice invoice = InvoiceController.getInstance().create("GROUP", "G01");
        assertEquals(List.of("Group: G01 - Familia"), invoice.getHeader());
        assertEquals(1, invoice.getSections().size());
        Invoice.Item item = invoice.getSections().get(0).items().get(0);
        assertEquals(3, item.details().size());
        assertEquals(new BigDecimal("108.00"), invoice.getTotal());
        assertThrows(NotFoundException.class, () -> InvoiceController.getInstance().create("GROUP", "G99"));
        assertThrows(InvalidDataException.class, () -> InvoiceController.getInstance().create("COMPANY", "X"));
    }

    // ---------------------------------------------------------- C09 persistence

    @Test
    void passesAndTypesSurviveRestart() throws Exception {
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createSpecialPass("PE01", "T001");
        park.createSpeedPasses("SP01", "TG01:87654321X", "1");
        park.enter("A04", "T001", null);

        ParkController reloaded = restart();
        Attraction special = reloaded.requireAttraction("A04");
        assertEquals(AttractionType.SPECIAL, special.getType());
        assertTrue(special.getOccupants().isEmpty());
        Pass pe = reloaded.getPasses().get(0);
        assertTrue(pe instanceof SpecialPass);
        assertTrue(pe.isConsumed());
        assertSame(reloaded.findTicket("T001"), pe.getTicket());
        Pass sp = reloaded.getPasses().get(1);
        assertEquals(new BigDecimal("7.20"), sp.getPrice());
        assertSame(((GroupTicket) reloaded.findTicket("TG01")).getComposition().get(1), sp.getTicket());
        assertFalse(sp.isConsumed());
    }

    @Test
    void legacyAndInvalidRecordsLoad() throws Exception {
        ByteArrayOutputStream warnings = new ByteArrayOutputStream();
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;20", "A02;Bad;5;VIP", "A03;Ok;5;GROUP"));
        Files.write(dir.resolve("passes.txt"), List.of("SPEED;S1;T001;8.00;false", "SPEED;S2;T999;8.00;false",
                "SPECIAL;P1;T001;40.00;true", "SPECIAL;P2;T001;40.00;false", "OTHER;X;T001;1.00;false",
                "SPEED;S3;T001;8.00;maybe", "SPEED;S1;T001;8.00;false"));
        ParkController reloaded = TestSupport.initialize(dir, new PrintStream(warnings, true));
        assertEquals(List.of(AttractionType.GENERAL, AttractionType.GROUP),
                reloaded.getAttractions().stream().map(Attraction::getType).toList());
        assertEquals(List.of("S1", "P1"), reloaded.getPasses().stream().map(Pass::getIdentifier).toList());
        assertEquals(6, warnings.toString().split("WARNING", -1).length - 1);
        IndividualTicket ticket = (IndividualTicket) reloaded.findTicket("T001");
        User ana = reloaded.requireUser("ana@correo.com");
        assertSame(ana, ticket.getUser());
    }
}
