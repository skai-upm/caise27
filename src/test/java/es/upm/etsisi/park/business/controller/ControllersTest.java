package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.group.Group;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.business.model.ticket.Ticket;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.business.model.user.UserField;
import es.upm.etsisi.park.support.TestContext;

class ControllersTest {

    static final String ANA = "ana@correo.com";
    static final String PEDRO = "pedro@correo.com";
    static final String LAURA = "laura@correo.com";
    static final String MARTA = "marta@correo.com";

    @TempDir
    Path dir;

    SystemController system = SystemController.getInstance();
    UserController users = UserController.getInstance();
    GroupController groups = GroupController.getInstance();
    TicketController tickets = TicketController.getInstance();
    AttractionController attractions = AttractionController.getInstance();

    @BeforeEach
    void setUp() throws Exception {
        TestContext.initialize(dir);
        users.create("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
        users.create("Laura", "Martin Ruiz", "11111111H", LAURA, "Laura3$", "634567890", "1111222233334444");
        users.create("Marta", "Sanchez Perez", "00000000T", MARTA, "Marta4%", "645678901", "2222333344445555");
        system.setDate("2027-04-15");
    }

    void reopen() throws Exception {
        TestContext.initialize(dir);
        system.setDate("2027-04-15");
    }

    Group groupOf(String id, String... emails) throws Exception {
        Group group = groups.create(id, "Group " + id);
        for (String email : emails) {
            groups.addMember(id, email);
        }
        return group;
    }

    @Nested
    class Settings {

        @Test
        void controllersAreSingletons() {
            assertSame(SystemController.getInstance(), SystemController.getInstance());
            assertSame(UserController.getInstance(), UserController.getInstance());
            assertSame(GroupController.getInstance(), GroupController.getInstance());
            assertSame(TicketController.getInstance(), TicketController.getInstance());
            assertSame(PassController.getInstance(), PassController.getInstance());
            assertSame(AttractionController.getInstance(), AttractionController.getInstance());
            assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        }

        @Test
        void defaultsOnOpen() throws Exception {
            system.setBasePrice("45.50");
            TestContext.initialize(dir);
            assertEquals(LocalDate.now(), system.getDate());
            assertEquals(new BigDecimal("40.00"), system.getBasePrice());
        }

        @Test
        void setsDate() throws Exception {
            assertEquals(LocalDate.of(2027, 4, 15), system.getDate());
            system.setDate("2027-05-01");
            assertEquals(LocalDate.of(2027, 5, 1), system.getDate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow"})
        void rejectsInvalidDate(String text) {
            assertThrows(ParkException.class, () -> system.setDate(text));
            assertEquals(LocalDate.of(2027, 4, 15), system.getDate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"45.50", "45.5", "45", "0.01"})
        void setsPrice(String text) throws Exception {
            assertEquals(new BigDecimal(text).setScale(2), system.setBasePrice(text));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-3", "0", "0.00", "abc", "1.999", "1e3", ".5", "5.", "1.2.3", ""})
        void rejectsInvalidPrice(String text) {
            assertThrows(ParkException.class, () -> system.setBasePrice(text));
            assertEquals(new BigDecimal("40.00"), system.getBasePrice());
        }

        @Test
        void priceChangeDoesNotAffectExistingTickets() throws Exception {
            IndividualTicket first = tickets.createTicket("T001", ANA, "2027-04-15");
            system.setBasePrice("45.50");
            IndividualTicket second = tickets.createTicket("T002", ANA, "2027-04-15");
            assertEquals(new BigDecimal("40.00"), first.getPrice());
            assertEquals(new BigDecimal("45.50"), second.getPrice());
        }
    }

    @Nested
    class Groups {

        @Test
        void createsEmptyGroup() throws Exception {
            groups.create("G01", "Familia Garcia");
            assertTrue(groups.find("g01").isEmpty());
            assertThrows(ParkException.class, () -> groups.create("G01", "Otro"));
            assertThrows(ParkException.class, () -> groups.create("G02", " "));
        }

        @Test
        void addsAndRemovesMembers() throws Exception {
            Group group = groupOf("G01", ANA, PEDRO);
            assertEquals(2, group.getMembers().size());
            assertThrows(ParkException.class, () -> groups.addMember("G01", ANA));
            assertThrows(ParkException.class, () -> groups.addMember("G01", "nobody@correo.com"));
            assertThrows(ParkException.class, () -> groups.addMember("G99", ANA));
            groups.removeMember("G01", ANA);
            assertFalse(group.contains(users.findByEmail(ANA)));
            assertThrows(ParkException.class, () -> groups.removeMember("G01", ANA));
        }

        @Test
        void userMayBeInSeveralGroups() throws Exception {
            groupOf("G01", ANA);
            groupOf("G02", ANA);
            assertEquals(2, groups.groupsOf(users.findByEmail(ANA)).size());
        }

        @Test
        void activeGroupTicketLocksGroup() throws Exception {
            Group group = groupOf("G01", ANA, PEDRO);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertTrue(groups.isLocked(group));
            assertThrows(ParkException.class, () -> groups.addMember("G01", LAURA));
            assertThrows(ParkException.class, () -> groups.removeMember("G01", ANA));
            assertThrows(ParkException.class, () -> groups.delete("G01"));

            system.setDate("2027-04-16");
            assertFalse(groups.isLocked(group));
            groups.addMember("G01", LAURA);
        }

        @Test
        void deletingFutureTicketUnlocksGroup() throws Exception {
            groupOf("G02", PEDRO, LAURA);
            tickets.createGroupTicket("TG02", "G02", "2027-04-20");
            assertThrows(ParkException.class, () -> groups.removeMember("G02", LAURA));
            tickets.deleteGroupTicket("TG02");
            groups.removeMember("G02", LAURA);
            groups.delete("G02");
            assertNull(groups.find("G02"));
        }

        @Test
        void deletedGroupKeepsHistoricalTickets() throws Exception {
            groupOf("G01", ANA, PEDRO);
            GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            system.setDate("2027-04-16");
            groups.delete("G01");
            assertNull(ticket.getGroup());
            assertSame(ticket, tickets.find("TG01"));
            assertEquals(2, ticket.getMembers().size());
            assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        }
    }

    @Nested
    class Tickets {

        @Test
        void createsIndividualTicket() throws Exception {
            IndividualTicket ticket = tickets.createTicket("T001", ANA, "2027-04-15");
            assertSame(users.findByEmail(ANA), ticket.getUser());
            assertEquals(new BigDecimal("40.00"), ticket.getPrice());
            assertSame(ticket, tickets.find("t001"));
        }

        @Test
        void rejectsInvalidIndividualTickets() throws Exception {
            tickets.createTicket("T001", ANA, "2027-04-15");
            assertThrows(ParkException.class, () -> tickets.createTicket("T001", PEDRO, "2027-04-15"));
            assertThrows(ParkException.class, () -> tickets.createTicket("T009", ANA, "2027-04-14"));
            assertThrows(ParkException.class, () -> tickets.createTicket("T009", "nobody@correo.com", "2027-04-15"));
            assertThrows(ParkException.class, () -> tickets.createTicket("T009", ANA, "2027-13-01"));
            assertThrows(ParkException.class, () -> tickets.createTicket("", ANA, "2027-04-15"));
            assertEquals(1, tickets.getTickets().size());
        }

        @Test
        void sharedIdentifierSpace() throws Exception {
            groupOf("G01", ANA);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> tickets.createTicket("TG01", ANA, "2027-04-15"));
            assertThrows(ParkException.class, () -> tickets.createTicket("TG01:12345678Z", ANA, "2027-04-15"));
            tickets.createTicket("T001", PEDRO, "2027-04-15");
            groupOf("G02", PEDRO);
            assertThrows(ParkException.class, () -> tickets.createGroupTicket("T001", "G02", "2027-04-15"));
        }

        @Test
        void deletesOnlyFutureIndividualTickets() throws Exception {
            tickets.createTicket("T001", ANA, "2027-04-15");
            tickets.createTicket("T002", MARTA, "2027-04-20");
            assertThrows(ParkException.class, () -> tickets.deleteTicket("T001"));
            tickets.deleteTicket("T002");
            assertNull(tickets.find("T002"));
            assertThrows(ParkException.class, () -> tickets.deleteTicket("T002"));
            system.setDate("2027-04-16");
            assertThrows(ParkException.class, () -> tickets.deleteTicket("T001"));
        }

        @Test
        void createsGroupTicketWithDerivedMembers() throws Exception {
            groupOf("G01", ANA, PEDRO, LAURA);
            GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertEquals(List.of("TG01:12345678Z", "TG01:87654321X", "TG01:11111111H"),
                    ticket.getMembers().stream().map(Ticket::getId).toList());
            assertSame(groups.find("G01"), ticket.getGroup());
            assertEquals(new BigDecimal("108.00"), ticket.getPrice());
            assertNotNull(tickets.find("TG01:11111111H"));
        }

        @Test
        void compositionDoesNotFollowGroupChanges() throws Exception {
            groupOf("G01", ANA, PEDRO);
            GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            system.setDate("2027-04-16");
            groups.addMember("G01", LAURA);
            assertEquals(2, ticket.getMembers().size());
        }

        @Test
        void rejectsInvalidGroupTickets() throws Exception {
            groups.create("G00", "Empty");
            assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG00", "G00", "2027-04-15"));
            groupOf("G01", ANA);
            assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG01", "G01", "2027-04-01"));
            assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG01", "G99", "2027-04-15"));
            assertTrue(tickets.getTickets().isEmpty());
        }

        @Test
        void derivedIdentifierConflictCreatesNothing() throws Exception {
            tickets.createTicket("TG05:12345678Z", ANA, "2027-04-15");
            groupOf("G01", PEDRO, ANA);
            assertThrows(ParkException.class, () -> tickets.createGroupTicket("TG05", "G01", "2027-04-15"));
            assertNull(tickets.find("TG05"));
            assertNull(tickets.find("TG05:87654321X"));
            assertEquals(1, tickets.getTickets().size());
        }

        @Test
        void memberTicketsCannotBeDeletedAlone() throws Exception {
            groupOf("G02", PEDRO, LAURA);
            tickets.createGroupTicket("TG02", "G02", "2027-04-20");
            ParkException e = assertThrows(ParkException.class, () -> tickets.deleteTicket("TG02:87654321X"));
            assertTrue(e.getMessage().contains("TG02"));
            assertThrows(ParkException.class, () -> tickets.deleteTicket("TG02"));
            assertThrows(ParkException.class, () -> tickets.deleteGroupTicket("TG02:87654321X"));
        }

        @Test
        void deletesFutureGroupTicketWithMembers() throws Exception {
            groupOf("G01", ANA, PEDRO);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            tickets.createGroupTicket("TG02", "G01", "2027-04-20");
            assertThrows(ParkException.class, () -> tickets.deleteGroupTicket("TG01"));
            tickets.deleteGroupTicket("TG02");
            assertNull(tickets.find("TG02"));
            assertNull(tickets.find("TG02:12345678Z"));
            assertNotNull(tickets.find("TG01"));
        }
    }

    @Nested
    class UserDeletion {

        @Test
        void activeIndividualTicketBlocksDeletion() throws Exception {
            tickets.createTicket("T002", MARTA, "2027-04-20");
            assertThrows(ParkException.class, () -> users.delete(MARTA, "Marta4%"));
            tickets.deleteTicket("T002");
            users.delete(MARTA, "Marta4%");
            assertNull(users.findByEmail(MARTA));
        }

        @Test
        void todayTicketBlocksDeletion() throws Exception {
            tickets.createTicket("T001", MARTA, "2027-04-15");
            assertThrows(ParkException.class, () -> users.delete(MARTA, "Marta4%"));
        }

        @Test
        void activeGroupTicketBlocksDeletion() throws Exception {
            groupOf("G01", ANA, PEDRO);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> users.delete(ANA, "Clave1!"));
            assertNotNull(users.findByEmail(ANA));
        }

        @Test
        void wrongPasswordStillReported() {
            ParkException e = assertThrows(ParkException.class, () -> users.delete(ANA, "Wrong1!"));
            assertTrue(e.getMessage().contains("Incorrect password"));
        }

        @Test
        void removesUserFromUnlockedGroupsAndKeepsHistory() throws Exception {
            Group group = groupOf("G02", PEDRO, LAURA);
            tickets.createTicket("T001", PEDRO, "2027-04-15");
            system.setDate("2027-04-16");
            users.delete(PEDRO, "Otra2#");
            assertNull(users.findByEmail(PEDRO));
            assertEquals(1, group.getMembers().size());
            IndividualTicket ticket = (IndividualTicket) tickets.find("T001");
            assertEquals("87654321X", ticket.getUser().getDni());
        }

        @Test
        void updatesStillWork() throws Exception {
            users.update(ANA, "Clave1!", UserField.PHONE, "699999999");
            assertEquals("699999999", users.findByEmail(ANA).getPhone());
        }
    }

    @Nested
    class Attractions {

        @Test
        void createsAttraction() throws Exception {
            Attraction<?> a = attractions.create("A01", "Dragon Rojo", "20", null);
            assertEquals(20, a.getCapacity());
            assertTrue(a.getOccupants().isEmpty());
            assertThrows(ParkException.class, () -> attractions.create("a01", "Otro", "5", null));
            assertThrows(ParkException.class, () -> attractions.create("A02", "X", "0", null));
            assertThrows(ParkException.class, () -> attractions.create("A02", "X", "-5", null));
            assertThrows(ParkException.class, () -> attractions.create("A02", "X", "abc", null));
        }

        @Test
        void individualEntryAndExit() throws Exception {
            Attraction<?> a = attractions.create("A02", "Torre del Cielo", "12", null);
            tickets.createTicket("T001", ANA, "2027-04-15");
            assertEquals(1, attractions.enter("A02", "T001", null).users().size());
            assertTrue(a.contains(users.findByEmail(ANA)));
            assertEquals(11, a.getAvailable());
            assertThrows(ParkException.class, () -> attractions.enter("A02", "T001", null).users().size());
            attractions.exit("A02", ANA);
            assertEquals(12, a.getAvailable());
            assertThrows(ParkException.class, () -> attractions.exit("A02", ANA));
        }

        @Test
        void groupEntryIsAllOrNothing() throws Exception {
            Attraction<?> small = attractions.create("A03", "Mini", "2", null);
            Attraction<?> big = attractions.create("A01", "Dragon Rojo", "20", null);
            groupOf("G01", ANA, PEDRO, LAURA);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> attractions.enter("A03", "TG01", null).users().size());
            assertTrue(small.getOccupants().isEmpty());
            assertEquals(3, attractions.enter("A01", "TG01", null).users().size());
            assertEquals(17, big.getAvailable());
            attractions.exit("A01", PEDRO);
            assertEquals(2, big.getOccupants().size());
        }

        @Test
        void memberTicketAdmitsItsUser() throws Exception {
            Attraction<?> a = attractions.create("A01", "Dragon Rojo", "20", null);
            groupOf("G01", ANA, PEDRO);
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            assertEquals(1, attractions.enter("A01", "TG01:12345678Z", null).users().size());
            assertTrue(a.contains(users.findByEmail(ANA)));
        }

        @Test
        void ticketMustBeForToday() throws Exception {
            attractions.create("A01", "Dragon Rojo", "20", null);
            tickets.createTicket("T002", ANA, "2027-04-20");
            assertThrows(ParkException.class, () -> attractions.enter("A01", "T002", null).users().size());
            assertThrows(ParkException.class, () -> attractions.enter("A01", "T999", null).users().size());
            assertThrows(ParkException.class, () -> attractions.enter("A99", "T002", null).users().size());
        }

        @Test
        void dateChangeDoesNotRemoveOccupants() throws Exception {
            Attraction<?> a = attractions.create("A01", "Dragon Rojo", "20", null);
            tickets.createTicket("T001", ANA, "2027-04-15");
            attractions.enter("A01", "T001", null).users().size();
            system.setDate("2027-04-16");
            assertTrue(a.contains(users.findByEmail(ANA)));
        }
    }

    @Nested
    class Persistence {

        @Test
        void restoresParkAfterRestart() throws Exception {
            groupOf("G01", ANA, PEDRO, LAURA);
            attractions.create("A01", "Dragon Rojo", "20", null);
            tickets.createTicket("T001", ANA, "2027-04-15");
            system.setBasePrice("45.50");
            tickets.createGroupTicket("TG01", "G01", "2027-04-16");
            attractions.enter("A01", "T001", null).users().size();

            reopen();

            Group group = groups.find("G01");
            assertEquals(3, group.getMembers().size());
            assertSame(users.findByEmail(ANA), group.getMembers().get(0));
            IndividualTicket t001 = (IndividualTicket) tickets.find("T001");
            assertSame(users.findByEmail(ANA), t001.getUser());
            assertEquals(new BigDecimal("40.00"), t001.getPrice());
            GroupTicket tg01 = (GroupTicket) tickets.find("TG01");
            assertSame(group, tg01.getGroup());
            assertEquals(3, tg01.getMembers().size());
            assertEquals(new BigDecimal("122.85"), tg01.getPrice());
            assertNotNull(tickets.find("TG01:11111111H"));
            Attraction<?> a01 = attractions.find("A01");
            assertEquals("Dragon Rojo", a01.getName());
            assertEquals(20, a01.getCapacity());
            assertTrue(a01.getOccupants().isEmpty());
            assertTrue(groups.isLocked(group));
        }

        @Test
        void historicalRecordsOfDeletedUserAndGroup() throws Exception {
            groupOf("G01", ANA, MARTA);
            tickets.createTicket("T001", MARTA, "2027-04-15");
            tickets.createGroupTicket("TG01", "G01", "2027-04-15");
            system.setDate("2027-04-16");
            groups.delete("G01");
            users.delete(MARTA, "Marta4%");
            users.create("Nueva", "Persona", "00000000T", "nueva@correo.com", "Nueva1!", "600000000",
                    "1234123412341234");

            reopen();

            assertNull(users.findByEmail(MARTA));
            IndividualTicket t001 = (IndividualTicket) tickets.find("T001");
            User former = t001.getUser();
            assertEquals(MARTA, former.getEmail());
            assertFalse(users.getUsers().contains(former));
            GroupTicket tg01 = (GroupTicket) tickets.find("TG01");
            assertNull(tg01.getGroup());
            assertEquals(2, tg01.getMembers().size());
            assertSame(former, ((IndividualTicket) tickets.find("TG01:00000000T")).getUser());
        }
    }
}
