package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.service.Park;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketCommandsTest {

    static final String CREATE_LAURA =
            "USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444";

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
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void purchasesGroupTicketWithDiscount() {
        assertEquals("GROUPTICKET CREATED. Tickets: 3, discount: 10%, total: 108.00 EUR",
                run("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        GroupTicket ticket = (GroupTicket) park.findTicket("TG01");
        assertSame(park.findGroup("G01"), ticket.getGroup());
        IndividualTicket ana = (IndividualTicket) park.findTicket("TG01:12345678Z");
        assertSame(park.findUserByEmail("ana@correo.com"), ana.getUser());
        assertEquals(new BigDecimal("36.00"), ana.getPrice());
    }

    @Test
    void usesCurrentPriceAsBase() {
        run("SYSTEM PRICE;45.50");
        run("GROUP CREATE;G02;Pareja");
        run("GROUP ADD;G02;pedro@correo.com");
        run("GROUP ADD;G02;laura@correo.com");
        assertEquals("GROUPTICKET CREATED. Tickets: 2, discount: 0%, total: 91.00 EUR",
                run("GROUPTICKET CREATE;TG02;G02;2027-04-20"));
    }

    @Test
    void rejectsInvalidPurchases() {
        run("GROUP CREATE;G03;Vacio");
        assertTrue(run("GROUPTICKET CREATE;TG03;G03;2027-04-15").startsWith("ERROR"));
        assertTrue(run("GROUPTICKET CREATE;TG04;G99;2027-04-15").startsWith("ERROR"));
        assertTrue(run("GROUPTICKET CREATE;TG05;G01;2027-04-14").startsWith("ERROR"));
        assertTrue(run("GROUPTICKET CREATE;TG06;G01;someday").startsWith("ERROR"));
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(run("GROUPTICKET CREATE;T001;G01;2027-04-15").startsWith("ERROR"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void sharedIdSpaceIncludesComponents() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(run("TICKET CREATE;TG01;ana@correo.com;2027-04-16").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-16").startsWith("ERROR"));
        assertTrue(run("GROUPTICKET CREATE;TG01:87654321X;G01;2027-04-16").startsWith("ERROR"));
    }

    @Test
    void derivedIdConflictCreatesNothing() {
        run("TICKET CREATE;TG04:87654321X;ana@correo.com;2027-04-20");
        assertTrue(run("GROUPTICKET CREATE;TG04;G01;2027-04-15").startsWith("ERROR"));
        assertNull(park.findTicket("TG04"));
        assertNull(park.findTicket("TG04:12345678Z"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        List<String> lines = Files.readAllLines(dir.resolve("tickets.txt"));
        assertEquals("GROUP;TG01;2027-04-15;G01;3", lines.get(0));
        assertEquals("MEMBER;TG01:12345678Z;2027-04-15;40.00;10;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                lines.get(1));
        assertEquals(4, lines.size());

        park = TestParks.open(dir);
        GroupTicket ticket = (GroupTicket) park.findTicket("TG01");
        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertEquals(new BigDecimal("108.00"), ticket.getPrice());
        assertSame(park.findUserByEmail("laura@correo.com"), ticket.getComposition().get(2).getUser());
    }

    @Test
    void malformedGroupTicketRecordsAreSkippedEntirely() throws IOException {
        Files.write(dir.resolve("tickets.txt"), List.of(
                "GROUP;TG01;2027-04-15;G01;2",
                "MEMBER;TG01:12345678Z;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "INDIVIDUAL;T001;2027-04-15;40.00;0;87654321X;pedro@correo.com;Pedro;Lopez Martin",
                "MEMBER;TG09:1;2027-04-15;40.00;0;1;x@correo.com;X;Y",
                "GROUP;TG02;2027-04-15;;1",
                "MEMBER;TG02:12345678Z;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "WEIRD;line"));
        park = TestParks.open(dir);
        assertNull(park.findTicket("TG01"));
        assertEquals(2, park.getTickets().size());
        assertNull(((GroupTicket) park.findTicket("TG02")).getGroup());
    }
}
