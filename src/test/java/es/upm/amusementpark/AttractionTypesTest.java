package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAccess;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpecialAttraction;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.TextFileRepositories;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypesTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() throws IOException {
        park.reset();
        park.setCurrentDate("2027-04-15");
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.registerUser(new User("pedro@correo.com", "87654321X"));
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL\n"
                + "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL\n"
                + "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP\n"
                + "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL\n");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sourceExamplesCreateTheFourTypes() throws IOException {
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A03").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(run("ATTRACTION STATUS;A04\n").contains("TYPE: SPECIAL"));
    }

    @Test
    void legacySyntaxAndUnknownType() throws IOException {
        String out = run("ATTRACTION CREATE;A05;Noria;10\nATTRACTION CREATE;A06;X;10;VIP\nATTRACTION CREATE;A07;X\n");
        assertEquals(AttractionType.GENERAL, park.findAttraction("A05").getType());
        assertTrue(out.contains("Error: unknown attraction type"));
        assertTrue(out.contains("Error: invalid arguments for ATTRACTION CREATE"));
        assertNull(park.findAttraction("A06"));
    }

    @Test
    void generalAcceptsBoth() {
        park.enterAttraction("A01", "T001");
        park.findAttraction("A01").removeOccupant(park.findUser("ana@correo.com"));
        assertEquals(2, park.enterAttraction("A01", "TG01").size());
    }

    @Test
    void individualRejectsGroupTicket() {
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01"));
        assertTrue(e.getMessage().contains("of type INDIVIDUAL does not accept ticket TG01"));
        assertTrue(park.findAttraction("A02").getOccupants().isEmpty());
        assertEquals(1, park.enterAttraction("A02", "T001").size());
    }

    @Test
    void groupRejectsIndividualTicket() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "T001"));
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertEquals(2, park.enterAttraction("A03", "TG01").size());
    }

    @Test
    void specialRequiresUnconsumedSpecialPass() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "TG01"));
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001"));
        assertTrue(e.getMessage().contains("has no unconsumed special pass"));
        park.buySpecialPass("PE01", "T001");
        assertEquals(1, park.enterAttraction("A04", "T001").size());
    }

    @Test
    void specialRejectsConsumedPass() {
        park.buySpecialPass("PE01", "T001").consume();
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001"));
    }

    @Test
    void genericParameterMatchesCapability() throws Exception {
        Map<Class<?>, Class<?>> expected = Map.of(
                GeneralAttraction.class, AccessTicket.class,
                IndividualAttraction.class, IndividualAccess.class,
                GroupAttraction.class, GroupAccess.class,
                SpecialAttraction.class, IndividualAccess.class);
        for (Map.Entry<Class<?>, Class<?>> entry : expected.entrySet()) {
            ParameterizedType superType = (ParameterizedType) entry.getKey().getGenericSuperclass();
            assertEquals(Attraction.class, superType.getRawType());
            assertEquals(entry.getValue(), superType.getActualTypeArguments()[0]);
        }
        Method enter = Attraction.class.getMethod("enter", AccessTicket.class, java.time.LocalDate.class,
                es.upm.amusementpark.business.model.PassLookup.class);
        assertEquals("T", enter.getGenericParameterTypes()[0].getTypeName());
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
    }

    @Test
    void typesPersistAndLegacyLinesAreGeneral() throws IOException {
        park.configure(TextFileRepositories.create(dir));
        park.persist();
        Files.writeString(dir.resolve("attractions.txt"), "A09;Vieja;5\n", StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
        park.reset();
        park.configure(TextFileRepositories.create(dir));
        assertTrue(park.load().isEmpty());
        for (String[] pair : List.of(new String[] {"A01", "GENERAL"}, new String[] {"A02", "INDIVIDUAL"},
                new String[] {"A03", "GROUP"}, new String[] {"A04", "SPECIAL"}, new String[] {"A09", "GENERAL"})) {
            assertEquals(pair[1], park.findAttraction(pair[0]).getType().name());
        }
    }

    @Test
    void helpListsTypedCreation() throws IOException {
        assertTrue(run("HELP\n").contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>"));
    }
}
