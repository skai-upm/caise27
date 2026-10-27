package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.park.ParkController;

/**
 * Restart scenarios: state written by one execution is rebuilt by the next one.
 */
class ParkPersistenceTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();

    @TempDir
    Path data;

    private String run(String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String input = String.join("\n", lines) + "\n";
        Main.run(new String[0], new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()), data);
        return out.toString(StandardCharsets.UTF_8);
    }

    private ParkController park() {
        return ParkController.getInstance();
    }

    private void setUpPark() {
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "SYSTEM PRICE;45.50",
            "TICKET CREATE;T001;ana@correo.com;" + TOMORROW,
            "SYSTEM PRICE;40",
            "GROUPTICKET CREATE;TG01;G01;" + TOMORROW,
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "TICKET CREATE;T002;luis@correo.com;" + TODAY,
            "ATTRACTION ENTER;A01;T002");
    }

    @Test
    void groupsTicketsAndAttractionsSurviveRestart() {
        setUpPark();
        String out = run("ATTRACTION STATUS;A01", "USER DELETE;ana@correo.com;Clave1!", "GROUP ADD;G01;x@correo.com");
        ParkController park = park();
        assertEquals(3, park.getGroups().findById("G01").getMembers().size());
        IndividualTicket t = (IndividualTicket) park.getTickets().findById("T001");
        assertEquals("45.50", t.getPrice().toPlainString());
        assertEquals(park.getUsers().findByDni("12345678Z"), t.getUser());
        GroupTicket gt = (GroupTicket) park.getTickets().findById("TG01");
        assertEquals(park.getGroups().findById("G01"), gt.getGroup());
        assertEquals("108.00", gt.getPrice().toPlainString());
        assertEquals("TG01:11111111H", gt.getComposition().get(2).getId());
        assertTrue(out.contains("Users inside: 0"), out);
        assertTrue(out.contains("ERROR: User has active tickets"), out);
        assertTrue(out.contains("ERROR: Group has active group tickets"), out);
        assertEquals(20, park.getAttractions().findById("A01").getCapacity());
    }

    @Test
    void historicalRecordsSurviveDeletionAndRestart() {
        setUpPark();
        String day = LocalDate.now().plusDays(2).toString();
        run("SYSTEM DATE;" + day, "USER DELETE;ana@correo.com;Clave1!", "GROUP DELETE;G01");
        run();
        ParkController park = park();
        assertNull(park.getUsers().findByDni("12345678Z"));
        assertNull(park.getGroups().findById("G01"));
        IndividualTicket t = (IndividualTicket) park.getTickets().findById("T001");
        assertEquals("12345678Z", t.getUser().getDni());
        GroupTicket gt = (GroupTicket) park.getTickets().findById("TG01");
        assertNull(gt.getGroup());
        assertEquals(3, gt.getComposition().size());
        assertEquals("108.00", gt.getPrice().toPlainString());
    }
}
