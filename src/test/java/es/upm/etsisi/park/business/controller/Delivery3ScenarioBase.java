package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestSupport;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * The Delivery 3 example state: Ana, Pedro and Laura in G01, typed attractions A01–A04,
 * ticket T001 for Ana and group ticket TG01 (3 members, 36.00 each), all dated today.
 */
abstract class Delivery3ScenarioBase {

    static final String TODAY = "2027-04-15";
    static final String FUTURE = "2027-04-20";

    @TempDir
    Path dir;

    ParkController park;

    @BeforeEach
    void setUpScenario() {
        park = TestSupport.park(dir);
        park.setCurrentDate(TODAY);
        park.setCurrentPrice("40.00");
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Laura", "Martin Ruiz", "11111111H", "laura@correo.com", "Laura3$", "634567890",
                "1111222233334444");
        park.createGroup("G01", "Familia Garcia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "laura@correo.com");
        park.createAttraction("A01", "Dragon Rojo", "20", "GENERAL");
        park.createAttraction("A02", "Montana Individual", "12", "INDIVIDUAL");
        park.createAttraction("A03", "Rafting Familiar", "12", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createTicket("T001", "ana@correo.com", TODAY);
        park.createGroupTicket("TG01", "G01", TODAY);
    }
}
