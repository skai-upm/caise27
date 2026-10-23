package es.upm.etsisi.park;

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

import es.upm.etsisi.park.model.attraction.Attraction;
import es.upm.etsisi.park.model.group.Group;
import es.upm.etsisi.park.model.ticket.GroupTicket;
import es.upm.etsisi.park.model.ticket.IndividualTicket;
import es.upm.etsisi.park.model.ticket.Ticket;
import es.upm.etsisi.park.persistence.ParkStorage;
import es.upm.etsisi.park.user.User;
import es.upm.etsisi.park.user.UserField;

class ParkTest {

    static final String ANA = "ana@correo.com";
    static final String PEDRO = "pedro@correo.com";
    static final String LAURA = "laura@correo.com";
    static final String MARTA = "marta@correo.com";

    @TempDir
    Path dir;

    Park park;

    @BeforeEach
    void setUp() throws Exception {
        park = Park.getInstance();
        park.open(new ParkStorage(dir, new PrintStream(new ByteArrayOutputStream())));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
        park.createUser("Laura", "Martin Ruiz", "11111111H", LAURA, "Laura3$", "634567890", "1111222233334444");
        park.createUser("Marta", "Sanchez Perez", "00000000T", MARTA, "Marta4%", "645678901", "2222333344445555");
        park.setSystemDate("2027-04-15");
    }

    void reopen() throws Exception {
        park.open(new ParkStorage(dir, new PrintStream(new ByteArrayOutputStream())));
        park.setSystemDate("2027-04-15");
    }

    Group groupOf(String id, String... emails) throws Exception {
        Group group = park.createGroup(id, "Group " + id);
        for (String email : emails) {
            park.addToGroup(id, email);
        }
        return group;
    }

    @Nested
    class Settings {

        @Test
        void singleInstance() {
            assertSame(Park.getInstance(), Park.getInstance());
        }

        @Test
        void defaultsOnOpen() throws Exception {
            park.setBasePrice("45.50");
            park.open(new ParkStorage(dir, new PrintStream(new ByteArrayOutputStream())));
            assertEquals(LocalDate.now(), park.getSystemDate());
            assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        }

        @Test
        void setsDate() throws Exception {
            assertEquals(LocalDate.of(2027, 4, 15), park.getSystemDate());
            park.setSystemDate("2027-05-01");
            assertEquals(LocalDate.of(2027, 5, 1), park.getSystemDate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow"})
        void rejectsInvalidDate(String text) {
            assertThrows(ParkException.class, () -> park.setSystemDate(text));
            assertEquals(LocalDate.of(2027, 4, 15), park.getSystemDate());
        }

        @ParameterizedTest
        @ValueSource(strings = {"45.50", "45.5", "45", "0.01"})
        void setsPrice(String text) throws Exception {
            assertEquals(new BigDecimal(text).setScale(2), park.setBasePrice(text));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-3", "0", "0.00", "abc", "1.999", "1e3", ".5", "5.", "1.2.3", ""})
        void rejectsInvalidPrice(String text) {
            assertThrows(ParkException.class, () -> park.setBasePrice(text));
            assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        }

        @Test
        void priceChangeDoesNotAffectExistingTickets() throws Exception {
            IndividualTicket first = park.createTicket("T001", ANA, "2027-04-15");
            park.setBasePrice("45.50");
            IndividualTicket second = park.createTicket("T002", ANA, "2027-04-15");
            assertEquals(new BigDecimal("40.00"), first.getPrice());
            assertEquals(new BigDecimal("45.50"), second.getPrice());
        }
    }

    @Nested
    class Groups {

        @Test
        void createsEmptyGroup() throws Exception {
            park.createGroup("G01", "Familia Garcia");
            assertTrue(park.findGroup("g01").isEmpty());
            assertThrows(ParkException.class, () -> park.createGroup("G01", "Otro"));
            assertThrows(ParkException.class, () -> park.createGroup("G02", " "));
        }

        @Test
        void addsAndRemovesMembers() throws Exception {
            Group group = groupOf("G01", ANA, PEDRO);
            assertEquals(2, group.getMembers().size());
            assertThrows(ParkException.class, () -> park.addToGroup("G01", ANA));
            assertThrows(ParkException.class, () -> park.addToGroup("G01", "nobody@correo.com"));
            assertThrows(ParkException.class, () -> park.addToGroup("G99", ANA));
            park.removeFromGroup("G01", ANA);
            assertFalse(group.contains(park.findUser(ANA)));
            assertThrows(ParkException.class, () -> park.removeFromGroup("G01", ANA));
        }

        @Test
        void userMayBeInSeveralGroups() throws Exception {
            groupOf("G01", ANA);
            groupOf("G02", ANA);
            assertEquals(2, park.groupsOf(park.findUser(ANA)).size());
        }

        @Test
        void activeGroupTicketLocksGroup() throws Exception {
            Group group = groupOf("G01", ANA, PEDRO);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertTrue(park.isGroupLocked(group));
            assertThrows(ParkException.class, () -> park.addToGroup("G01", LAURA));
            assertThrows(ParkException.class, () -> park.removeFromGroup("G01", ANA));
            assertThrows(ParkException.class, () -> park.deleteGroup("G01"));

            park.setSystemDate("2027-04-16");
            assertFalse(park.isGroupLocked(group));
            park.addToGroup("G01", LAURA);
        }

        @Test
        void deletingFutureTicketUnlocksGroup() throws Exception {
            groupOf("G02", PEDRO, LAURA);
            park.createGroupTicket("TG02", "G02", "2027-04-20");
            assertThrows(ParkException.class, () -> park.removeFromGroup("G02", LAURA));
            park.deleteGroupTicket("TG02");
            park.removeFromGroup("G02", LAURA);
            park.deleteGroup("G02");
            assertNull(park.findGroup("G02"));
        }

        @Test
        void deletedGroupKeepsHistoricalTickets() throws Exception {
            groupOf("G01", ANA, PEDRO);
            GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
            park.setSystemDate("2027-04-16");
            park.deleteGroup("G01");
            assertNull(ticket.getGroup());
            assertSame(ticket, park.findTicket("TG01"));
            assertEquals(2, ticket.getMembers().size());
            assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        }
    }

    @Nested
    class Tickets {

        @Test
        void createsIndividualTicket() throws Exception {
            IndividualTicket ticket = park.createTicket("T001", ANA, "2027-04-15");
            assertSame(park.findUser(ANA), ticket.getUser());
            assertEquals(new BigDecimal("40.00"), ticket.getPrice());
            assertSame(ticket, park.findTicket("t001"));
        }

        @Test
        void rejectsInvalidIndividualTickets() throws Exception {
            park.createTicket("T001", ANA, "2027-04-15");
            assertThrows(ParkException.class, () -> park.createTicket("T001", PEDRO, "2027-04-15"));
            assertThrows(ParkException.class, () -> park.createTicket("T009", ANA, "2027-04-14"));
            assertThrows(ParkException.class, () -> park.createTicket("T009", "nobody@correo.com", "2027-04-15"));
            assertThrows(ParkException.class, () -> park.createTicket("T009", ANA, "2027-13-01"));
            assertThrows(ParkException.class, () -> park.createTicket("", ANA, "2027-04-15"));
            assertEquals(1, park.getTickets().size());
        }

        @Test
        void sharedIdentifierSpace() throws Exception {
            groupOf("G01", ANA);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> park.createTicket("TG01", ANA, "2027-04-15"));
            assertThrows(ParkException.class, () -> park.createTicket("TG01:12345678Z", ANA, "2027-04-15"));
            park.createTicket("T001", PEDRO, "2027-04-15");
            groupOf("G02", PEDRO);
            assertThrows(ParkException.class, () -> park.createGroupTicket("T001", "G02", "2027-04-15"));
        }

        @Test
        void deletesOnlyFutureIndividualTickets() throws Exception {
            park.createTicket("T001", ANA, "2027-04-15");
            park.createTicket("T002", MARTA, "2027-04-20");
            assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
            park.deleteTicket("T002");
            assertNull(park.findTicket("T002"));
            assertThrows(ParkException.class, () -> park.deleteTicket("T002"));
            park.setSystemDate("2027-04-16");
            assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        }

        @Test
        void createsGroupTicketWithDerivedMembers() throws Exception {
            groupOf("G01", ANA, PEDRO, LAURA);
            GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertEquals(List.of("TG01:12345678Z", "TG01:87654321X", "TG01:11111111H"),
                    ticket.getMembers().stream().map(Ticket::getId).toList());
            assertSame(park.findGroup("G01"), ticket.getGroup());
            assertEquals(new BigDecimal("108.00"), ticket.getPrice());
            assertNotNull(park.findTicket("TG01:11111111H"));
        }

        @Test
        void compositionDoesNotFollowGroupChanges() throws Exception {
            groupOf("G01", ANA, PEDRO);
            GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
            park.setSystemDate("2027-04-16");
            park.addToGroup("G01", LAURA);
            assertEquals(2, ticket.getMembers().size());
        }

        @Test
        void rejectsInvalidGroupTickets() throws Exception {
            park.createGroup("G00", "Empty");
            assertThrows(ParkException.class, () -> park.createGroupTicket("TG00", "G00", "2027-04-15"));
            groupOf("G01", ANA);
            assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", "2027-04-01"));
            assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G99", "2027-04-15"));
            assertTrue(park.getTickets().isEmpty());
        }

        @Test
        void derivedIdentifierConflictCreatesNothing() throws Exception {
            park.createTicket("TG05:12345678Z", ANA, "2027-04-15");
            groupOf("G01", PEDRO, ANA);
            assertThrows(ParkException.class, () -> park.createGroupTicket("TG05", "G01", "2027-04-15"));
            assertNull(park.findTicket("TG05"));
            assertNull(park.findTicket("TG05:87654321X"));
            assertEquals(1, park.getTickets().size());
        }

        @Test
        void memberTicketsCannotBeDeletedAlone() throws Exception {
            groupOf("G02", PEDRO, LAURA);
            park.createGroupTicket("TG02", "G02", "2027-04-20");
            ParkException e = assertThrows(ParkException.class, () -> park.deleteTicket("TG02:87654321X"));
            assertTrue(e.getMessage().contains("TG02"));
            assertThrows(ParkException.class, () -> park.deleteTicket("TG02"));
            assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG02:87654321X"));
        }

        @Test
        void deletesFutureGroupTicketWithMembers() throws Exception {
            groupOf("G01", ANA, PEDRO);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            park.createGroupTicket("TG02", "G01", "2027-04-20");
            assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
            park.deleteGroupTicket("TG02");
            assertNull(park.findTicket("TG02"));
            assertNull(park.findTicket("TG02:12345678Z"));
            assertNotNull(park.findTicket("TG01"));
        }
    }

    @Nested
    class UserDeletion {

        @Test
        void activeIndividualTicketBlocksDeletion() throws Exception {
            park.createTicket("T002", MARTA, "2027-04-20");
            assertThrows(ParkException.class, () -> park.deleteUser(MARTA, "Marta4%"));
            park.deleteTicket("T002");
            park.deleteUser(MARTA, "Marta4%");
            assertNull(park.findUser(MARTA));
        }

        @Test
        void todayTicketBlocksDeletion() throws Exception {
            park.createTicket("T001", MARTA, "2027-04-15");
            assertThrows(ParkException.class, () -> park.deleteUser(MARTA, "Marta4%"));
        }

        @Test
        void activeGroupTicketBlocksDeletion() throws Exception {
            groupOf("G01", ANA, PEDRO);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> park.deleteUser(ANA, "Clave1!"));
            assertNotNull(park.findUser(ANA));
        }

        @Test
        void wrongPasswordStillReported() {
            ParkException e = assertThrows(ParkException.class, () -> park.deleteUser(ANA, "Wrong1!"));
            assertTrue(e.getMessage().contains("Incorrect password"));
        }

        @Test
        void removesUserFromUnlockedGroupsAndKeepsHistory() throws Exception {
            Group group = groupOf("G02", PEDRO, LAURA);
            park.createTicket("T001", PEDRO, "2027-04-15");
            park.setSystemDate("2027-04-16");
            park.deleteUser(PEDRO, "Otra2#");
            assertNull(park.findUser(PEDRO));
            assertEquals(1, group.getMembers().size());
            IndividualTicket ticket = (IndividualTicket) park.findTicket("T001");
            assertEquals("87654321X", ticket.getUser().getDni());
        }

        @Test
        void updatesStillWork() throws Exception {
            park.updateUser(ANA, "Clave1!", UserField.PHONE, "699999999");
            assertEquals("699999999", park.findUser(ANA).getPhone());
        }
    }

    @Nested
    class Attractions {

        @Test
        void createsAttraction() throws Exception {
            Attraction a = park.createAttraction("A01", "Dragon Rojo", "20");
            assertEquals(20, a.getCapacity());
            assertTrue(a.getOccupants().isEmpty());
            assertThrows(ParkException.class, () -> park.createAttraction("a01", "Otro", "5"));
            assertThrows(ParkException.class, () -> park.createAttraction("A02", "X", "0"));
            assertThrows(ParkException.class, () -> park.createAttraction("A02", "X", "-5"));
            assertThrows(ParkException.class, () -> park.createAttraction("A02", "X", "abc"));
        }

        @Test
        void individualEntryAndExit() throws Exception {
            Attraction a = park.createAttraction("A02", "Torre del Cielo", "12");
            park.createTicket("T001", ANA, "2027-04-15");
            assertEquals(1, park.enterAttraction("A02", "T001"));
            assertTrue(a.contains(park.findUser(ANA)));
            assertEquals(11, a.getAvailable());
            assertThrows(ParkException.class, () -> park.enterAttraction("A02", "T001"));
            park.exitAttraction("A02", ANA);
            assertEquals(12, a.getAvailable());
            assertThrows(ParkException.class, () -> park.exitAttraction("A02", ANA));
        }

        @Test
        void groupEntryIsAllOrNothing() throws Exception {
            Attraction small = park.createAttraction("A03", "Mini", "2");
            Attraction big = park.createAttraction("A01", "Dragon Rojo", "20");
            groupOf("G01", ANA, PEDRO, LAURA);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertThrows(ParkException.class, () -> park.enterAttraction("A03", "TG01"));
            assertTrue(small.getOccupants().isEmpty());
            assertEquals(3, park.enterAttraction("A01", "TG01"));
            assertEquals(17, big.getAvailable());
            park.exitAttraction("A01", PEDRO);
            assertEquals(2, big.getOccupants().size());
        }

        @Test
        void memberTicketAdmitsItsUser() throws Exception {
            Attraction a = park.createAttraction("A01", "Dragon Rojo", "20");
            groupOf("G01", ANA, PEDRO);
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            assertEquals(1, park.enterAttraction("A01", "TG01:12345678Z"));
            assertTrue(a.contains(park.findUser(ANA)));
        }

        @Test
        void ticketMustBeForToday() throws Exception {
            park.createAttraction("A01", "Dragon Rojo", "20");
            park.createTicket("T002", ANA, "2027-04-20");
            assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002"));
            assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T999"));
            assertThrows(ParkException.class, () -> park.enterAttraction("A99", "T002"));
        }

        @Test
        void dateChangeDoesNotRemoveOccupants() throws Exception {
            Attraction a = park.createAttraction("A01", "Dragon Rojo", "20");
            park.createTicket("T001", ANA, "2027-04-15");
            park.enterAttraction("A01", "T001");
            park.setSystemDate("2027-04-16");
            assertTrue(a.contains(park.findUser(ANA)));
        }
    }

    @Nested
    class Persistence {

        @Test
        void restoresParkAfterRestart() throws Exception {
            groupOf("G01", ANA, PEDRO, LAURA);
            park.createAttraction("A01", "Dragon Rojo", "20");
            park.createTicket("T001", ANA, "2027-04-15");
            park.setBasePrice("45.50");
            park.createGroupTicket("TG01", "G01", "2027-04-16");
            park.enterAttraction("A01", "T001");

            reopen();

            Group group = park.findGroup("G01");
            assertEquals(3, group.getMembers().size());
            assertSame(park.findUser(ANA), group.getMembers().get(0));
            IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
            assertSame(park.findUser(ANA), t001.getUser());
            assertEquals(new BigDecimal("40.00"), t001.getPrice());
            GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
            assertSame(group, tg01.getGroup());
            assertEquals(3, tg01.getMembers().size());
            assertEquals(new BigDecimal("122.85"), tg01.getPrice());
            assertNotNull(park.findTicket("TG01:11111111H"));
            Attraction a01 = park.findAttraction("A01");
            assertEquals("Dragon Rojo", a01.getName());
            assertEquals(20, a01.getCapacity());
            assertTrue(a01.getOccupants().isEmpty());
            assertTrue(park.isGroupLocked(group));
        }

        @Test
        void historicalRecordsOfDeletedUserAndGroup() throws Exception {
            groupOf("G01", ANA, MARTA);
            park.createTicket("T001", MARTA, "2027-04-15");
            park.createGroupTicket("TG01", "G01", "2027-04-15");
            park.setSystemDate("2027-04-16");
            park.deleteGroup("G01");
            park.deleteUser(MARTA, "Marta4%");
            park.createUser("Nueva", "Persona", "00000000T", "nueva@correo.com", "Nueva1!", "600000000",
                    "1234123412341234");

            reopen();

            assertNull(park.findUser(MARTA));
            IndividualTicket t001 = (IndividualTicket) park.findTicket("T001");
            User former = t001.getUser();
            assertEquals(MARTA, former.getEmail());
            assertFalse(park.getUsers().contains(former));
            GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
            assertNull(tg01.getGroup());
            assertEquals(2, tg01.getMembers().size());
            assertSame(former, ((IndividualTicket) park.findTicket("TG01:00000000T")).getUser());
        }
    }
}
