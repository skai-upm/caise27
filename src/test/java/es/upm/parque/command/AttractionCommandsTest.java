package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.model.Attraction;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;
import es.upm.parque.repository.UserRepository;

class AttractionCommandsTest {

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserRepository());
        park = ParkController.getInstance();
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457");
    }

    @Test
    void createsEmptyAttraction() {
        assertTrue(processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20").startsWith("OK"));
        Attraction a01 = park.getAttractions().findById("A01");
        assertEquals("Dragon Rojo", a01.getName());
        assertEquals(20, a01.getCapacity());
        assertTrue(a01.getOccupants().isEmpty());
    }

    @Test
    void rejectsDuplicateAndInvalidCreation() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertTrue(processor.process("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertEquals(20, park.getAttractions().findById("A01").getCapacity());
        for (String bad : new String[] {"ATTRACTION CREATE;A02;Torre;0", "ATTRACTION CREATE;A02;Torre;-3",
                "ATTRACTION CREATE;A02;Torre;doce", "ATTRACTION CREATE;A02;Torre;1.5",
                "ATTRACTION CREATE;;Torre;12", "ATTRACTION CREATE;A02;;12", "ATTRACTION CREATE;A02;Torre",
                "ATTRACTION CREATE;A02;Torre;9999999999", "ATTRACTION CREATE;A02;Torre;"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertNull(park.getAttractions().findById("A02"));
    }

    @Test
    void listsEmptyAttractionWithoutChangingIt() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        String r = processor.process("ATTRACTION LIST;A01");
        assertTrue(r.startsWith("OK") && r.contains("no users inside"), r);
        assertTrue(park.getAttractions().findById("A01").getOccupants().isEmpty());
        assertTrue(processor.process("ATTRACTION LIST;A99").startsWith("ERROR"));
        assertTrue(processor.process("ATTRACTION LIST").startsWith("ERROR"));
    }

    @Test
    void listsOccupantEmails() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        Attraction a01 = park.getAttractions().findById("A01");
        User ana = park.getUsers().findByEmail("ana@correo.com");
        assertTrue(a01.addOccupant(ana));
        assertFalse(a01.addOccupant(ana));
        a01.addOccupant(park.getUsers().findByEmail("pedro@correo.com"));
        String r = processor.process("ATTRACTION LIST;A01");
        assertTrue(r.startsWith("OK") && r.contains("ana@correo.com") && r.contains("pedro@correo.com"), r);
        assertEquals(2, a01.getOccupants().size());
        assertTrue(a01.isInside(ana));
        assertTrue(a01.removeOccupant(ana));
        assertFalse(a01.isInside(ana));
        assertThrows(UnsupportedOperationException.class, () -> a01.getOccupants().clear());
    }

    @Test
    void usersHoldNoAttractionReference() {
        for (Field field : User.class.getDeclaredFields()) {
            assertFalse(Attraction.class.isAssignableFrom(field.getType()), field.getName());
        }
    }

    @Test
    void attractionsResetPerExecutionAndHelpListsCommands() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        String help = processor.process("HELP");
        assertTrue(help.contains("ATTRACTION CREATE;<id>;<name>;<capacity>"));
        assertTrue(help.contains("ATTRACTION LIST;<id>"));
        new CommandProcessor(new UserRepository());
        assertTrue(park.getAttractions().findAll().isEmpty());
    }
}
