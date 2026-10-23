package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.attraction.Entry;
import es.upm.etsisi.park.business.model.attraction.GroupAttraction;
import es.upm.etsisi.park.business.model.attraction.IndividualAttraction;
import es.upm.etsisi.park.business.model.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.park.business.model.invoice.Invoice;
import es.upm.etsisi.park.business.model.invoice.UserInvoiceBuilder;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.Access;
import es.upm.etsisi.park.business.model.ticket.GroupAccess;
import es.upm.etsisi.park.business.model.ticket.IndividualAccess;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.support.TestContext;

/** Passes, typed attraction access and invoices (Delivery 3), following the assignment's example. */
class Delivery3Test {

    static final String ANA = "ana@correo.com";
    static final String PEDRO = "pedro@correo.com";
    static final String LAURA = "laura@correo.com";

    @TempDir
    Path dir;

    final SystemController system = SystemController.getInstance();
    final UserController users = UserController.getInstance();
    final GroupController groups = GroupController.getInstance();
    final TicketController tickets = TicketController.getInstance();
    final PassController passes = PassController.getInstance();
    final AttractionController attractions = AttractionController.getInstance();
    final InvoiceController invoices = InvoiceController.getInstance();

    @BeforeEach
    void setUp() throws Exception {
        TestContext.initialize(dir);
        system.setDate("2027-04-15");
        users.create("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
        users.create("Laura", "Martin Ruiz", "11111111H", LAURA, "Laura3$", "634567890", "1111222233334444");
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", ANA);
        groups.addMember("G01", PEDRO);
        groups.addMember("G01", LAURA);
        attractions.create("A01", "Dragon Rojo", "20", "GENERAL");
        attractions.create("A02", "Montana Individual", "12", "INDIVIDUAL");
        attractions.create("A03", "Rafting Familiar", "12", "GROUP");
        attractions.create("A04", "Pasaje del Terror", "15", "SPECIAL");
        tickets.createTicket("T001", ANA, "2027-04-15");
        tickets.createGroupTicket("TG01", "G01", "2027-04-15");
    }

    IndividualTicket ticket(String id) {
        return (IndividualTicket) tickets.find(id);
    }

    @Nested
    class Passes {

        @Test
        void packageOfTenPricedAtTwentyPercent() throws Exception {
            system.setBasePrice("50.00");
            List<SpeedPass> created = passes.createSpeedPasses("SP01", "T001", "10");
            assertEquals(10, created.size());
            assertEquals("SP01:1", created.get(0).getId());
            assertEquals("SP01:10", created.get(9).getId());
            for (SpeedPass pass : created) {
                assertEquals(new BigDecimal("8.00"), pass.getPrice());
                assertFalse(pass.isConsumed());
            }
            assertEquals(10, ticket("T001").getSpeedPasses().size());
        }

        @Test
        void singlePassAndDiscountedMemberTicket() throws Exception {
            List<SpeedPass> created = passes.createSpeedPasses("SP02", "TG01:12345678Z", "1");
            assertEquals("SP02:1", created.get(0).getId());
            assertEquals(new BigDecimal("7.20"), created.get(0).getPrice());
        }

        @Test
        void rejectsInvalidPurchases() throws Exception {
            assertThrows(ValidationException.class, () -> passes.createSpeedPasses("SP01", "T001", "5"));
            assertThrows(NotFoundException.class, () -> passes.createSpeedPasses("SP01", "T999", "1"));
            assertThrows(ValidationException.class, () -> passes.createSpeedPasses("SP01", "TG01", "1"));
            system.setDate("2027-04-16");
            assertThrows(OperationNotAllowedException.class, () -> passes.createSpeedPasses("SP01", "T001", "1"));
            assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE01", "T001"));
            assertTrue(passes.getPasses().isEmpty());
        }

        @Test
        void conflictCreatesNothing() throws Exception {
            tickets.createTicket("T002", PEDRO, "2027-04-20");
            passes.createSpeedPasses("SP03", "T002", "1");
            passes.createSpecialPass("SP04:7", "T002");
            assertThrows(DuplicateException.class, () -> passes.createSpeedPasses("SP04", "T001", "10"));
            assertNull(passes.find("SP04:1"));
            assertTrue(ticket("T001").getSpeedPasses().isEmpty());
        }

        @Test
        void specialPassIsUniquePerTicket() throws Exception {
            SpecialPass pass = passes.createSpecialPass("PE01", "T001");
            assertEquals(new BigDecimal("40.00"), pass.getPrice());
            assertSame(pass, ticket("T001").getSpecialPass());
            assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE02", "T001"));
            assertThrows(DuplicateException.class, () -> passes.createSpecialPass("pe01", "TG01:12345678Z"));
            pass.consume();
            assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE02", "T001"));
        }

        @Test
        void refundsOnlyUnconsumedPassesOfFutureTickets() throws Exception {
            tickets.createTicket("T005", ANA, "2027-04-20");
            passes.createSpeedPasses("SP05", "T005", "10");
            passes.createSpecialPass("PE05", "T005");
            passes.createSpeedPasses("SP06", "T001", "1");

            passes.deleteSpeedPass("SP05:3");
            assertNull(passes.find("SP05:3"));
            assertEquals(9, ticket("T005").getSpeedPasses().size());
            assertThrows(NotFoundException.class, () -> passes.deleteSpecialPass("SP05:4"));
            assertThrows(NotFoundException.class, () -> passes.deleteSpeedPass("PE05"));
            assertThrows(OperationNotAllowedException.class, () -> passes.deleteSpeedPass("SP06:1"));
            passes.find("SP05:4").consume();
            assertThrows(OperationNotAllowedException.class, () -> passes.deleteSpeedPass("SP05:4"));
            passes.deleteSpecialPass("PE05");
            assertNull(ticket("T005").getSpecialPass());
            passes.createSpecialPass("PE06", "T005");
        }

        @Test
        void ticketDeletionCascadesToPasses() throws Exception {
            tickets.createTicket("T002", PEDRO, "2027-04-20");
            passes.createSpeedPasses("SP07", "T002", "1");
            passes.createSpeedPasses("SP08", "T002", "10");
            passes.createSpecialPass("PE07", "T002");
            tickets.deleteTicket("T002");
            assertNull(passes.find("SP07:1"));
            assertNull(passes.find("SP08:10"));
            assertNull(passes.find("PE07"));
            assertTrue(passes.getPasses().isEmpty());
        }

        @Test
        void groupTicketDeletionCascadesToMemberPasses() throws Exception {
            groups.create("G02", "Visita");
            groups.addMember("G02", PEDRO);
            groups.addMember("G02", LAURA);
            tickets.createGroupTicket("TG02", "G02", "2027-04-20");
            passes.createSpeedPasses("SP09", "TG02:87654321X", "1");
            passes.createSpecialPass("PE09", "TG02:11111111H");
            passes.createSpeedPasses("SP10", "T001", "1");
            tickets.deleteGroupTicket("TG02");
            assertNull(passes.find("SP09:1"));
            assertNull(passes.find("PE09"));
            assertNotNull(passes.find("SP10:1"));
        }

        @Test
        void failedTicketDeletionKeepsPasses() throws Exception {
            passes.createSpeedPasses("SP11", "T001", "1");
            assertThrows(OperationNotAllowedException.class, () -> tickets.deleteTicket("T001"));
            assertNotNull(passes.find("SP11:1"));
        }
    }

    @Nested
    class Access {

        @Test
        void attractionTypesAndGenerics() throws Exception {
            assertEquals(AttractionType.SPECIAL, attractions.find("A04").getType());
            assertEquals(AttractionType.GENERAL, attractions.create("A05", "Legacy", "5", null).getType());
            assertThrows(ValidationException.class, () -> attractions.create("A06", "X", "10", "VIP"));
            Attraction<IndividualAccess> individual = new IndividualAttraction("X1", "x", 5);
            Attraction<GroupAccess> group = new GroupAttraction("X2", "x", 5);
            assertTrue(individual.accepts(ticket("T001")));
            assertFalse(individual.accepts(tickets.find("TG01")));
            assertTrue(group.accepts(tickets.find("TG01")));
            assertFalse(group.accepts(ticket("T001")));
        }

        @Test
        void typeRestrictionsOnEntry() throws Exception {
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A02", "TG01", null));
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A03", "T001", null));
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "TG01", null));
            assertTrue(attractions.find("A02").getOccupants().isEmpty());
            assertEquals(1, attractions.enter("A02", "TG01:87654321X", null).users().size());
        }

        @Test
        void individualSpeedEntryConsumesExactlyOne() throws Exception {
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A02", "T001", "SPEED"));
            passes.createSpeedPasses("SP01", "T001", "10");
            Entry entry = attractions.enter("A02", "T001", "speed");
            assertEquals(1, entry.consumedPasses().size());
            long consumed = ticket("T001").getSpeedPasses().stream().filter(Pass::isConsumed).count();
            assertEquals(1, consumed);
            assertTrue(entry.consumedPasses().get(0).isConsumed());
        }

        @Test
        void groupSpeedEntryIsAtomic() throws Exception {
            passes.createSpeedPasses("SP02", "TG01:12345678Z", "1");
            passes.createSpeedPasses("SP03", "TG01:87654321X", "1");
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A03", "TG01", "SPEED"));
            assertTrue(attractions.find("A03").getOccupants().isEmpty());
            assertFalse(passes.find("SP02:1").isConsumed());
            assertFalse(passes.find("SP03:1").isConsumed());

            passes.createSpeedPasses("SP04", "TG01:11111111H", "1");
            Entry entry = attractions.enter("A03", "TG01", "SPEED");
            assertEquals(3, entry.users().size());
            assertEquals(3, entry.consumedPasses().size());
            assertTrue(passes.find("SP02:1").isConsumed());
            assertTrue(passes.find("SP04:1").isConsumed());
        }

        @Test
        void capacityFailureConsumesNothing() throws Exception {
            attractions.create("A07", "Mini", "2", "GROUP");
            passes.createSpeedPasses("SP02", "TG01:12345678Z", "1");
            passes.createSpeedPasses("SP03", "TG01:87654321X", "1");
            passes.createSpeedPasses("SP04", "TG01:11111111H", "1");
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A07", "TG01", "SPEED"));
            assertFalse(passes.find("SP02:1").isConsumed());
        }

        @Test
        void specialAttractionConsumesSpecialPass() throws Exception {
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001", null));
            passes.createSpecialPass("PE01", "T001");
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001", "SPEED"));
            assertFalse(passes.find("PE01").isConsumed());
            Entry entry = attractions.enter("A04", "T001", null);
            assertSame(passes.find("PE01"), entry.consumedPasses().get(0));
            assertTrue(passes.find("PE01").isConsumed());
            attractions.exit("A04", ANA);
            assertThrows(OperationNotAllowedException.class, () -> attractions.enter("A04", "T001", null));
        }

        @Test
        void invalidOption() {
            assertThrows(ValidationException.class, () -> attractions.enter("A01", "T001", "FAST"));
        }

        @Test
        void consumptionIsPersisted() throws Exception {
            passes.createSpeedPasses("SP01", "T001", "1");
            attractions.enter("A01", "T001", "SPEED");
            TestContext.initialize(dir);
            assertTrue(passes.find("SP01:1").isConsumed());
            assertEquals(new BigDecimal("8.00"), passes.find("SP01:1").getPrice());
            assertSame(tickets.find("T001"), passes.find("SP01:1").getTicket());
            assertEquals(AttractionType.SPECIAL, attractions.find("A04").getType());
            assertTrue(Files.readString(dir.resolve("passes.txt")).contains("SPEED;SP01:1;T001;8.00;true"));
        }
    }

    @Nested
    class Invoices {

        @Test
        void userInvoiceExample() throws Exception {
            passes.createSpeedPasses("SP01", "T001", "10");
            passes.createSpecialPass("PE01", "T001");
            passes.createSpeedPasses("SP02", "TG01:12345678Z", "1");
            attractions.enter("A04", "T001", null);

            Invoice invoice = invoices.create("user", ANA);
            assertEquals(new BigDecimal("167.20"), invoice.getTotal());
            assertEquals(1, invoice.findSection(UserInvoiceBuilder.TICKETS).lines().size());
            assertEquals(11, invoice.findSection(UserInvoiceBuilder.SPEED_PASSES).lines().size());
            assertEquals(1, invoice.findSection(UserInvoiceBuilder.SPECIAL_PASSES).lines().size());
            String text = invoice.toText();
            assertTrue(text.contains("Customer: Ana Garcia Lopez"));
            assertTrue(text.contains("Ticket T001"));
            assertFalse(text.contains("Ticket TG01:12345678Z"));
            assertTrue(text.contains("Special Pass PE01 for T001 [consumed]"));
            assertTrue(text.contains("167.20 EUR"));
        }

        @Test
        void sectionsWithoutDataAreOmitted() throws Exception {
            Invoice pedro = invoices.userInvoice(PEDRO);
            assertTrue(pedro.getSections().isEmpty());
            assertEquals(new BigDecimal("0.00"), pedro.getTotal());
            Invoice ana = invoices.userInvoice(ANA);
            assertEquals(1, ana.getSections().size());
            assertNull(ana.findSection(UserInvoiceBuilder.SPEED_PASSES));
        }

        @Test
        void groupInvoiceCountsGroupTicketsOnce() throws Exception {
            passes.createSpeedPasses("SP02", "TG01:12345678Z", "10");
            Invoice invoice = invoices.create("GROUP", "g01");
            assertEquals(new BigDecimal("108.00"), invoice.getTotal());
            assertEquals(4, invoice.findSection(GroupInvoiceBuilder.GROUP_TICKETS).lines().size());
            String text = invoice.toText();
            assertTrue(text.contains("Group: G01 - Familia Garcia"));
            assertTrue(text.contains("TG01:11111111H"));
            assertFalse(text.contains("SpeedPass"));
        }

        @Test
        void invoiceErrors() {
            assertThrows(NotFoundException.class, () -> invoices.create("USER", "nobody@correo.com"));
            assertThrows(NotFoundException.class, () -> invoices.create("GROUP", "G99"));
            ValidationException e = assertThrows(ValidationException.class, () -> invoices.create("SHOP", "X"));
            assertTrue(e.getMessage().contains("USER") && e.getMessage().contains("GROUP"));
        }
    }
}
