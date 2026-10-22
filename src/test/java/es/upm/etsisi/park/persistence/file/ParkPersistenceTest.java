package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    @Test
    void parkStateSurvivesRestart() {
        Park park = TestPark.withUsers(dir);
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
        park.getAttractionController().create("A01", "Dragon Rojo", "20", null);
        park.getSystemController().setPrice("45.50");
        park.getTicketController().createIndividual("T001", "marta@correo.com", "2027-04-20");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        park.getAttractionController().enter("A01", "TG01", null);

        TestPark.configure(dir);

        Group group = park.getRepositories().groups().findById("G01").orElseThrow();
        assertEquals(3, group.getMembers().size());
        assertSame(park.getRepositories().users().findByEmail("ana@correo.com").orElseThrow(), group.getMembers().get(0));

        IndividualTicket t001 = (IndividualTicket) park.getRepositories().tickets().findById("T001").orElseThrow();
        assertSame(park.getRepositories().users().findByEmail("marta@correo.com").orElseThrow(), t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());
        assertEquals(LocalDate.of(2027, 4, 20), t001.getDate());

        GroupTicket tg01 = (GroupTicket) park.getRepositories().tickets().findById("TG01").orElseThrow();
        assertSame(group, tg01.getGroup());
        assertEquals(3, tg01.getTickets().size());
        assertEquals(new BigDecimal("122.85"), tg01.getPrice());
        assertTrue(park.getRepositories().tickets().findById("TG01:12345678Z").isPresent());

        assertEquals(20, park.getRepositories().attractions().findById("A01").orElseThrow().getCapacity());
        assertTrue(park.getRepositories().attractions().findById("A01").orElseThrow().getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), park.getState().getBasePrice());
    }

    @Test
    void historicalRecordsOfDeletedUsersAndGroupsSurviveRestart() {
        Park park = TestPark.withUsers(dir);
        park.getGroupController().create("G01", "Familia");
        park.getGroupController().addMember("G01", "laura@correo.com");
        park.getTicketController().createIndividual("T001", "marta@correo.com", "2027-04-15");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        park.getSystemController().setDate("2027-04-16");
        park.getGroupController().delete("G01");
        park.getUserController().delete("marta@correo.com", "Marta4%");
        park.getUserController().delete("laura@correo.com", "Laura3$");

        TestPark.configure(dir);

        IndividualTicket t001 = (IndividualTicket) park.getRepositories().tickets().findById("T001").orElseThrow();
        assertEquals("00000000T", t001.getUser().getDni());
        assertEquals("marta@correo.com", t001.getUser().getEmail());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park.getRepositories().tickets().findById("TG01").orElseThrow();
        assertNull(tg01.getGroup());
        assertEquals("11111111H", tg01.getTickets().get(0).getUser().getDni());
        assertTrue(park.getRepositories().users().findByEmail("marta@correo.com").isEmpty());
    }

    @Test
    void passesAndAttractionTypesSurviveRestart() throws IOException {
        Park park = TestPark.withUsers(dir);
        park.getAttractionController().create("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getPassController().buySpecialPass("PE01", "T001");
        park.getPassController().buySpeedPasses("SP01", "T001", "1");
        park.getAttractionController().enter("A04", "T001", null);

        TestPark.configure(dir);

        assertEquals(AttractionType.SPECIAL, park.getAttractionController().find("A04").getType());
        Pass special = park.getRepositories().passes().findById("PE01").orElseThrow();
        Pass speed = park.getRepositories().passes().findById("SP01:1").orElseThrow();
        assertTrue(special instanceof SpecialPass && special.isConsumed());
        assertTrue(speed instanceof SpeedPass && !speed.isConsumed());
        assertEquals(new BigDecimal("8.00"), speed.getPrice());
        assertSame(park.getTicketController().find("T001"), speed.getTicket());
        assertTrue(Files.readString(dir.resolve("attractions.txt")).contains("A04;Pasaje del Terror;15;SPECIAL"));
    }

    @Test
    void deliveryTwoAttractionLinesAreGeneral() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon Rojo;20"));

        Park park = TestPark.empty(dir);

        assertEquals(AttractionType.GENERAL, park.getAttractionController().find("A01").getType());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Dragon;20", "bad", "A02;Torre;x"));
        Files.write(dir.resolve("tickets.txt"), List.of("INDIVIDUAL;T1;not-a-date;40.00;0;1;a;b;c", "???"));
        Files.write(dir.resolve("groups.txt"), List.of("G01;Fam;99999999R", "broken"));
        Files.write(dir.resolve("passes.txt"), List.of("SPEED;SP1:1;T404;8.00;false", "BOGUS;X;T1;1;false"));

        Park park = TestPark.empty(dir);

        assertEquals(1, park.getRepositories().attractions().findAll().size());
        assertTrue(park.getRepositories().tickets().findAll().isEmpty());
        assertTrue(park.getRepositories().groups().findById("G01").orElseThrow().isEmpty());
        assertTrue(park.getRepositories().passes().findAll().isEmpty());
    }
}
