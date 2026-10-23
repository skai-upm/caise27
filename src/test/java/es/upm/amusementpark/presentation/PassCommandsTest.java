package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassCommandsTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void buysPackageOfTen() {
        assertEquals("SPEEDPASS CREATED. Passes: SP02:1 to SP02:10, unit price: 8.00 EUR, total: 80.00 EUR",
                run("SPEEDPASS CREATE;SP02;T001;10"));
        for (int i = 1; i <= 10; i++) {
            Pass pass = park.findPass("SP02:" + i);
            assertEquals(new BigDecimal("8.00"), pass.getPrice());
            assertTrue(pass.getTicket() == park.findTicket("T001"));
        }
        assertNull(park.findPass("SP02:11"));
    }

    @Test
    void buysSinglePassForGroupComponentAtDiscountedPrice() {
        assertEquals("SPEEDPASS CREATED. Passes: SP01:1, unit price: 7.20 EUR, total: 7.20 EUR",
                run("SPEEDPASS CREATE;SP01;TG01:12345678Z;1"));
        assertEquals("SPECIALPASS CREATED. Price: 36.00 EUR", run("SPECIALPASS CREATE;PE09;TG01:87654321X"));
    }

    @Test
    void laterPriceChangesDoNotAffectPasses() {
        run("SYSTEM PRICE;45.50");
        assertEquals("SPECIALPASS CREATED. Price: 40.00 EUR", run("SPECIALPASS CREATE;PE01;T001"));
    }

    @Test
    void rejectsInvalidSpeedPassPurchases() {
        assertTrue(run("SPEEDPASS CREATE;SP03;T001;5").startsWith("ERROR"));
        assertTrue(run("SPEEDPASS CREATE;SP03;T001;0").startsWith("ERROR"));
        assertTrue(run("SPEEDPASS CREATE;SP03;NOPE;1").startsWith("ERROR"));
        assertTrue(run("SPEEDPASS CREATE;SP03;TG01;1").startsWith("ERROR"));
        assertTrue(run("SPEEDPASS CREATE;;T001;1").startsWith("ERROR"));
        run("SYSTEM DATE;2027-04-16");
        assertTrue(run("SPEEDPASS CREATE;SP03;T001;1").startsWith("ERROR"));
        assertNull(park.findPass("SP03:1"));
    }

    @Test
    void identifierConflictCreatesNothing() {
        run("SPEEDPASS CREATE;SP04;T002;10");
        run("SPEEDPASS DELETE;SP04:1");
        assertTrue(run("SPEEDPASS CREATE;SP04;T001;10").startsWith("ERROR"));
        assertNull(park.findPass("SP04:1"));
        assertEquals(9, park.passes().getPasses().size());
        run("SPECIALPASS CREATE;PE01;T001");
        assertTrue(run("SPECIALPASS CREATE;PE01;T002").startsWith("ERROR"));
        assertTrue(run("SPEEDPASS CREATE;X;T001;1").startsWith("SPEEDPASS CREATED"));
        assertTrue(run("SPECIALPASS CREATE;T001;T002").startsWith("SPECIALPASS CREATED"),
                "pass identifiers do not share the ticket identifier space");
    }

    @Test
    void onlyOneSpecialPassPerTicketEver() {
        run("SPECIALPASS CREATE;PE01;T001");
        assertTrue(run("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        park.findPass("PE01").consume();
        assertTrue(run("SPECIALPASS CREATE;PE02;T001").startsWith("ERROR"));
        assertNull(park.findPass("PE02"));
        assertTrue(run("SPECIALPASS CREATE;PE03;TG01").startsWith("ERROR"));
    }

    @Test
    void refundRules() {
        run("SPEEDPASS CREATE;SP02;T002;10");
        run("SPECIALPASS CREATE;PE02;T002");
        run("SPECIALPASS CREATE;PE01;T001");
        assertEquals("SPEEDPASS DELETED", run("SPEEDPASS DELETE;SP02:3"));
        assertNull(park.findPass("SP02:3"));
        assertNotNull(park.findPass("SP02:1"));
        park.findPass("SP02:1").consume();
        assertTrue(run("SPEEDPASS DELETE;SP02:1").startsWith("ERROR"));
        assertTrue(run("SPECIALPASS DELETE;PE01").startsWith("ERROR"), "ticket dated today");
        assertTrue(run("SPEEDPASS DELETE;PE02").startsWith("ERROR"), "wrong kind of pass");
        assertTrue(run("SPECIALPASS DELETE;SP02:2").startsWith("ERROR"), "wrong kind of pass");
        assertTrue(run("SPEEDPASS DELETE;NOPE").startsWith("ERROR"));
        assertEquals("SPECIALPASS DELETED", run("SPECIALPASS DELETE;PE02"));
        assertTrue(run("SPECIALPASS CREATE;PE03;T002").startsWith("SPECIALPASS CREATED"),
                "a refunded Special Pass no longer exists");
    }

    @Test
    void passesSurviveRestart() throws IOException {
        run("SPEEDPASS CREATE;SP01;T001;1");
        run("SPECIALPASS CREATE;PE01;TG01:11111111H");
        assertEquals(List.of("SPEED;SP01:1;T001;8.00;false", "SPECIAL;PE01;TG01:11111111H;36.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));
        Park reopened = TestParks.open(dir);
        SpecialPass special = (SpecialPass) reopened.findPass("PE01");
        assertTrue(special.getTicket() == reopened.findTicket("TG01:11111111H"));
        assertEquals(new BigDecimal("36.00"), special.getPrice());
    }

    @Test
    void malformedPassLinesAreSkipped() throws IOException {
        Files.write(dir.resolve("passes.txt"), List.of(
                "SPEED;SP01:1;T001;8.00;true",
                "SPEED;SP01:1;T001;8.00;false",
                "SPECIAL;PE01;T001;40.00;false",
                "SPECIAL;PE02;T001;40.00;false",
                "SPEED;SP09:1;NOPE;8.00;false",
                "SPEED;SP09:2;TG01;8.00;false",
                "VIP;V1;T001;1.00;false",
                "SPEED;SP09:3;T001;8.00;maybe"));
        Park reopened = TestParks.open(dir);
        assertEquals(2, reopened.passes().getPasses().size());
        assertTrue(reopened.findPass("SP01:1").isConsumed());
    }
}
