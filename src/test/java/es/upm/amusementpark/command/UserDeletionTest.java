package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.service.Park;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * USER DELETE rules that depend on tickets and groups.
 */
class UserDeletionTest {

    static final String CREATE_MARTA =
            "USER CREATE;Marta;Sanchez Perez;00000000T;marta@correo.com;Marta4%;645678901;2222333344445555";

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_MARTA);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void activeTicketTodayBlocksDeletion() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertNotNull(park.findUserByEmail("ana@correo.com"));
    }

    @Test
    void futureTicketBlocksUntilDeleted() {
        run("SYSTEM PRICE;45.50");
        run("TICKET CREATE;T002;marta@correo.com;2027-04-20");
        assertTrue(run("USER DELETE;marta@correo.com;Marta4%").startsWith("ERROR"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertEquals("USER DELETED", run("USER DELETE;marta@correo.com;Marta4%"));
        assertNull(park.findUserByEmail("marta@correo.com"));
    }

    @Test
    void historicalTicketsDoNotBlockAndAreKept() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", run("USER DELETE;ana@correo.com;Clave1!"));
        assertNull(park.findUserByEmail("ana@correo.com"));
        IndividualTicket kept = (IndividualTicket) park.findTicket("T001");
        assertEquals("12345678Z", kept.getUser().getDni());

        Park reopened = TestParks.open(dir, LocalDate.of(2027, 4, 16));
        IndividualTicket reloaded = (IndividualTicket) reopened.findTicket("T001");
        User former = reloaded.getUser();
        assertEquals("12345678Z", former.getDni());
        assertEquals("ana@correo.com", former.getEmail());
        assertEquals("Ana", former.getFirstName());
        assertEquals("Garcia Lopez", former.getSurnames());
        assertEquals(new BigDecimal("40.00"), reloaded.getPrice());
        assertNull(reopened.findUserByEmail("ana@correo.com"));
        assertEquals(1, reopened.getUsers().size());
    }

    @Test
    void reregisteredUserIsLinkedByDniAfterRestart() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("SYSTEM DATE;2027-04-16");
        run("USER DELETE;ana@correo.com;Clave1!");
        run(CREATE_ANA);
        assertNotSame(park.findUserByEmail("ana@correo.com"), ((IndividualTicket) park.findTicket("T001")).getUser());

        Park reopened = TestParks.open(dir, LocalDate.of(2027, 4, 16));
        assertTrue(((IndividualTicket) reopened.findTicket("T001")).getUser()
                == reopened.findUserByEmail("ana@correo.com"));
    }

    @Test
    void wrongPasswordIsCheckedBeforeTickets() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        String output = run("USER DELETE;ana@correo.com;Wrong1!");
        assertTrue(output.contains("Incorrect password"));
    }
}
