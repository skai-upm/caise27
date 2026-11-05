package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.ParkController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GeneralAccess;
import es.upm.parque.business.model.GroupAccess;
import es.upm.parque.business.model.GroupAttraction;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualAccess;
import es.upm.parque.business.model.IndividualAttraction;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialAttraction;
import es.upm.parque.persistence.ParkFileStore;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.persistence.UserRepository;

class AttractionTypesTest {

    @TempDir
    Path dir;

    private CommandProcessor start() {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkConfiguration.initialize(ParkRepositories.fileBacked(users, new ParkFileStore(dir)),
                TestSupport.FAST_PASSWORDS);
        return new CommandProcessor();
    }

    private CommandProcessor park() {
        CommandProcessor p = start();
        for (String line : List.of(
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "SYSTEM DATE;2027-04-01",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T002;pedro@correo.com;2027-04-15",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "SPECIALPASS CREATE;PE01;T001",
                "SYSTEM DATE;2027-04-15")) {
            p.process(line);
        }
        return p;
    }

    private static Attraction<?> attraction(String id) {
        return ParkController.getInstance().getAttractions().findById(id);
    }

    @Test
    void attractionsAreCreatedWithTheirType() {
        CommandProcessor p = start();
        assertEquals("OK: attraction A01 (Dragon Rojo) created with capacity 20, type GENERAL",
                p.process("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL"));
        assertTrue(p.process("ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL").endsWith("type INDIVIDUAL"));
        assertTrue(p.process("ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP").endsWith("type GROUP"));
        assertTrue(p.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL").endsWith("type SPECIAL"));
        assertTrue(p.process("ATTRACTION CREATE;A05;Noria;10").endsWith("type GENERAL"), "Delivery 2 syntax");
        assertEquals(AttractionType.SPECIAL, attraction("A04").getType());
        assertTrue(attraction("A02") instanceof IndividualAttraction);
    }

    @Test
    void unknownOrEmptyTypeIsRejected() {
        CommandProcessor p = start();
        assertTrue(p.process("ATTRACTION CREATE;A06;Torre;10;VIP").startsWith("ERROR: invalid attraction type"));
        assertTrue(p.process("ATTRACTION CREATE;A06;Torre;10;").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION CREATE;A06;Torre;10;general").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION CREATE;A06;Torre;10;GENERAL;x").startsWith("ERROR"));
        assertNull(attraction("A06"));
    }

    @Test
    void generalAttractionAcceptsBothTicketTypes() {
        CommandProcessor p = park();
        assertTrue(p.process("ATTRACTION ENTER;A01;T002").startsWith("OK"));
        assertTrue(p.process("ATTRACTION ENTER;A01;TG01").startsWith("OK: 1 user(s)"), "Pedro was already inside");
    }

    @Test
    void individualAttractionRejectsGroupTickets() {
        CommandProcessor p = park();
        assertEquals("ERROR: attraction A02 (INDIVIDUAL) does not accept group tickets",
                p.process("ATTRACTION ENTER;A02;TG01"));
        assertTrue(attraction("A02").getOccupants().isEmpty());
        assertTrue(p.process("ATTRACTION ENTER;A02;T002").startsWith("OK"));
    }

    @Test
    void groupAttractionAcceptsOnlyGroupTickets() {
        CommandProcessor p = park();
        assertEquals("ERROR: attraction A03 (GROUP) does not accept individual tickets",
                p.process("ATTRACTION ENTER;A03;T001"));
        assertTrue(p.process("ATTRACTION ENTER;A03;TG01").startsWith("OK: 2 user(s)"));
        assertEquals(2, attraction("A03").getOccupants().size());
    }

    @Test
    void specialAttractionRequiresAnUnconsumedSpecialPass() {
        CommandProcessor p = park();
        assertTrue(p.process("ATTRACTION ENTER;A04;TG01").contains("does not accept group tickets"));
        String noPass = p.process("ATTRACTION ENTER;A04;T002");
        assertTrue(noPass.startsWith("ERROR") && noPass.contains("no Special Pass"), noPass);
        assertTrue(p.process("ATTRACTION ENTER;A04;T001").startsWith("OK"));
        p.process("ATTRACTION EXIT;A04;ana@correo.com");
        ParkController.getInstance().getPasses().findById("PE01").consume();
        String consumed = p.process("ATTRACTION ENTER;A04;T001");
        assertTrue(consumed.startsWith("ERROR") && consumed.contains("already been consumed"), consumed);
        assertTrue(attraction("A04").getOccupants().isEmpty());
    }

    @Test
    void commonEntryRulesStillApplyAfterTheTypeCheck() {
        CommandProcessor p = park();
        p.process("SYSTEM DATE;2027-04-16");
        assertTrue(p.process("ATTRACTION ENTER;A02;T001").contains("is valid on 2027-04-15"));
        assertTrue(p.process("ATTRACTION ENTER;A99;T001").startsWith("ERROR: no attraction"));
        assertTrue(p.process("ATTRACTION ENTER;A02;T999").startsWith("ERROR: no ticket"));
    }

    @Test
    void genericParameterRestrictsTheTicketsAnAttractionHandles() {
        CommandProcessor p = park();
        p.process("SYSTEM DATE;2027-04-15");
        IndividualTicket t001 = (IndividualTicket) ParkController.getInstance().getTickets().findById("T001");
        GroupTicket tg01 = (GroupTicket) ParkController.getInstance().getTickets().findById("TG01");
        IndividualAttraction individual = new IndividualAttraction("X1", "X", 5);
        GroupAttraction group = new GroupAttraction("X2", "Y", 5);
        IndividualAccess typed = individual.accept(t001);
        assertSame(t001, typed);
        assertSame(t001, typed.individualTicket());
        assertNull(individual.accept(tg01));
        GroupAccess groupTyped = group.accept(tg01);
        assertEquals(2, groupTyped.getComposition().size());
        assertNull(group.accept(t001));
        assertNull(new SpecialAttraction("X3", "Z", 5).accept(tg01));
        GeneralAccess general = tg01;
        assertEquals(2, general.individualTickets().size());
        assertEquals(List.of(t001), ((GeneralAccess) t001).individualTickets());
    }

    @Test
    void typesArePersistedAndTypelessLinesAreGeneral() throws IOException {
        park();
        List<String> lines = Files.readAllLines(dir.resolve(ParkFileStore.ATTRACTIONS_FILE), StandardCharsets.UTF_8);
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL", "A02;Montana Rusa Individual;12;INDIVIDUAL",
                "A03;Rafting Familiar;24;GROUP", "A04;Pasaje del Terror;15;SPECIAL"), lines);
        start();
        assertEquals(AttractionType.GROUP, attraction("A03").getType());
        assertEquals(AttractionType.SPECIAL, attraction("A04").getType());

        Files.write(dir.resolve(ParkFileStore.ATTRACTIONS_FILE),
                List.of("A01;Dragon Rojo;20", "A02;Bad;5;VIP", "A03;Rafting;24;GROUP"), StandardCharsets.UTF_8);
        start();
        assertNotNull(attraction("A01"));
        assertEquals(AttractionType.GENERAL, attraction("A01").getType());
        assertEquals(20, attraction("A01").getCapacity());
        assertNull(attraction("A02"), "an unknown type is a malformed line");
        assertEquals(AttractionType.GROUP, attraction("A03").getType());
    }
}
