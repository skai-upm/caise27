package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.repository.UserRepository;

class AttractionCommandsTest {

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.start(new UserRepository());
        processor = new CommandProcessor(park);
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
    }

    @Test
    void createsAttractions() {
        assertEquals("ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A02;Torre del Cielo;12"));
        Attraction a = park.getAttractions().findById("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertEquals(0, a.occupancy());
        assertEquals(12, park.getAttractions().findById("A02").getCapacity());
    }

    @Test
    void rejectsDuplicatesAndInvalidCapacity() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertEquals("ERROR: Attraction already exists", processor.process("ATTRACTION CREATE;A01;Otra;5"));
        assertEquals("Dragon Rojo", park.getAttractions().findById("A01").getName());
        for (String bad : new String[] {"0", "-3", "abc", "2.5", "9999999999"}) {
            assertTrue(processor.process("ATTRACTION CREATE;A03;X;" + bad).startsWith("ERROR"), bad);
        }
        assertTrue(processor.process("ATTRACTION CREATE;A03;X").startsWith("ERROR"));
        assertNull(park.getAttractions().findById("A03"));
    }

    @Test
    void listsOccupantsWithoutChangingState() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertEquals("No users in A01", processor.process("ATTRACTION LIST;A01"));
        Attraction a = park.getAttractions().findById("A01");
        a.enter(park.getUsers().findByEmail("ana@correo.com"));
        a.enter(park.getUsers().findByEmail("pedro@correo.com"));
        assertEquals("Users in A01:\nana@correo.com\npedro@correo.com", processor.process("ATTRACTION LIST;A01"));
        assertEquals(2, a.occupancy());
        assertEquals("ERROR: Attraction not found", processor.process("ATTRACTION LIST;A99"));
    }

    @Test
    void helpListsAttractionCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>"));
        assertTrue(help.contains("ATTRACTION LIST;<attraction>"));
    }
}
