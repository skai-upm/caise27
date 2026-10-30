package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ParkPersistenceTest extends ParkTestSupport {

    @Test
    void fullStateSurvivesRestart() {
        groupWith("G01", 3);
        user(9);
        tickets.createIndividual("T001", email(9), "2027-04-15");
        system.setPrice("50");
        tickets.createGroup("TG01", "G01", "2027-04-16");
        attractions.create("A01", "Dragon Rojo", "20");

        restart();

        Group group = park.getGroups().findById("G01");
        assertEquals(3, group.getMembers().size());
        assertSame(park.getUsers().findByEmail(email(1)), group.getMembers().get(0));

        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001");
        assertSame(park.getUsers().findByEmail(email(9)), t001.getUser());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        assertEquals(LocalDate.of(2027, 4, 15), t001.getDate());

        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01");
        assertSame(group, tg01.getGroup());
        assertEquals(3, tg01.getComposition().size());
        assertEquals(new BigDecimal("135.00"), tg01.getPrice());
        assertEquals("TG01:" + dni(2), tg01.getComposition().get(1).getId());
        assertSame(park.getUsers().findByEmail(email(2)), tg01.getComposition().get(1).getUser());

        assertEquals(20, park.getAttractions().findById("A01").getCapacity());
    }

    @Test
    void attractionsStartEmpty() {
        attractions.create("A01", "Dragon Rojo", "20");
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        attractions.enter("A01", "T001");
        restart();
        assertTrue(park.getAttractions().findById("A01").getOccupants().isEmpty());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurvive() {
        groupWith("G01", 2);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        tickets.createGroup("TG01", "G01", "2027-04-10");
        system.setDate("2027-04-11");
        users.delete(email(1), "Clave1!");
        groups.delete("G01");

        restart();

        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001");
        assertEquals(dni(1), t001.getUser().getDni());
        assertEquals(email(1), t001.getUser().getEmail());
        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01");
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getComposition().size());
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());
        assertEquals(dni(1), tg01.getComposition().get(0).getUser().getDni());
    }

    @Test
    void formerUserIsNotRelinkedToNewUserWithSameDni() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        system.setDate("2027-04-11");
        users.delete(email(1), "Clave1!");
        User newcomer = users.create("Nuevo", "Usuario", dni(1), "nuevo@correo.com", "Clave1!", "600000000",
                "1234567890123456");
        restart();
        User holder = ((IndividualTicket) park.getTickets().findById("T001")).getUser();
        assertEquals(email(1), holder.getEmail());
        assertNotSame(park.getUsers().findByDni(newcomer.getDni()), holder);
    }

    @Test
    void passesAndAttractionTypesSurviveRestart() {
        attractions.create("A04", "Pasaje del Terror", "15", "SPECIAL");
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        passes.buySpecialPass("PE01", "T001");
        passes.buySpeedPasses("SP01", "T001", "1");
        passes.buySpeedPasses("SP02", "T001", "1");
        attractions.enter("A04", "T001");
        attractions.exit("A04", email(1));
        attractions.create("A01", "Dragon Rojo", "20");
        attractions.enter("A01", "T001", true);

        restart();

        assertEquals(AttractionType.SPECIAL, park.getAttractions().findById("A04").getType());
        assertEquals(AttractionType.GENERAL, park.getAttractions().findById("A01").getType());
        Pass special = park.getPasses().findById("PE01");
        assertTrue(special instanceof SpecialPass && special.isConsumed());
        assertSame(park.getTickets().findById("T001"), special.getTicket());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertTrue(park.getPasses().findById("SP01:1").isConsumed());
        assertFalse(park.getPasses().findById("SP02:1").isConsumed());
        assertEquals(new BigDecimal("8.00"), park.getPasses().findById("SP02:1").getPrice());
    }

    @Test
    void deliveryTwoAttractionLineIsGeneral() throws java.io.IOException {
        java.nio.file.Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\n");
        restart();
        assertEquals(AttractionType.GENERAL, park.getAttractions().findById("A01").getType());
    }
}
