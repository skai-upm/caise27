package es.upm.etsisi.amusementpark.business;

import static es.upm.etsisi.amusementpark.TestSupport.ANA;
import static es.upm.etsisi.amusementpark.TestSupport.PEDRO;
import static es.upm.etsisi.amusementpark.TestSupport.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.ApplicationContext;
import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.TestSupport.MemoryRepository;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Controllers work with an injected in-memory repository and undo changes that cannot be saved. */
class RollbackTest {

    private MemoryRepository repository;
    private CommandProcessor processor;
    private Park park;

    @BeforeEach
    void setUp() throws Exception {
        repository = new MemoryRepository();
        processor = ApplicationContext.start(repository, TestSupport.TODAY, TestSupport.HASHER);
        park = Park.getInstance();
        run(processor, ANA, PEDRO, "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20", "TICKET CREATE;T1;ana@correo.com;2027-04-15",
                "TICKET CREATE;T2;pedro@correo.com;2027-04-20", "SPEEDPASS CREATE;S;T1;1",
                "SPEEDPASS CREATE;R;T2;1", "SPECIALPASS CREATE;Q;T2", "ATTRACTION CREATE;A01;X;5",
                "GROUP CREATE;G02;Other", "TICKET CREATE;T3;pedro@correo.com;2027-04-15", "ATTRACTION ENTER;A01;T3");
    }

    @Test
    void injectedRepositoryReceivesState() {
        assertTrue(repository.saves > 0);
        assertEquals(2, repository.stored.users().size());
        assertEquals(3, repository.stored.passes().size());
    }

    @Test
    void failedSavesChangeNothing() {
        int users = park.users().size();
        int tickets = park.tickets().size();
        int passes = park.passes().size();
        repository.failOnSave = true;
        List<String> outputs = run(processor,
                TestSupport.user(5), "USER UPDATE;ana@correo.com;Clave1!;PHONE;1",
                "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#", "GROUP CREATE;G09;X",
                "GROUP ADD;G02;ana@correo.com", "GROUP REMOVE;G01;ana@correo.com", "GROUP DELETE;G02",
                "TICKET CREATE;T9;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG9;G01;2027-04-15",
                "TICKET DELETE;T2", "GROUPTICKET DELETE;TG01", "SPEEDPASS CREATE;S9;T1;10",
                "SPECIALPASS CREATE;Q9;T1", "SPEEDPASS DELETE;R:1", "SPECIALPASS DELETE;Q",
                "ATTRACTION CREATE;A09;Y;5", "ATTRACTION ENTER;A01;T1;SPEED", "USER DELETE;pedro@correo.com;Otra2#");
        outputs.forEach(o -> assertTrue(o.startsWith("ERROR:"), o));
        assertEquals(users, park.users().size());
        assertEquals(tickets, park.tickets().size());
        assertEquals(passes, park.passes().size());
        assertEquals("612345678", park.users().get(0).getPhone());
        assertTrue(park.findGroup("G02").isPresent());
        assertEquals(1, park.findGroup("G01").orElseThrow().getMembers().size());
        assertFalse(park.findPass("S:1").orElseThrow().isConsumed());
        assertEquals(1, park.findAttraction("A01").orElseThrow().getOccupants().size());
        repository.failOnSave = false;
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;1").output());
    }
}
