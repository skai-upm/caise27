package es.upm.etsisi.amusementpark.presentation;

import static es.upm.etsisi.amusementpark.TestSupport.ANA;
import static es.upm.etsisi.amusementpark.TestSupport.LUIS;
import static es.upm.etsisi.amusementpark.TestSupport.PEDRO;
import static es.upm.etsisi.amusementpark.TestSupport.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.park.Park;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    @TempDir
    Path dir;

    private CommandProcessor processor;
    private Park park;

    @BeforeEach
    void setUp() throws Exception {
        processor = TestSupport.start(dir);
        park = Park.getInstance();
    }

    private String run1(String line) {
        return processor.process(line).output();
    }

    @Test
    void userLifecycle() {
        assertEquals("USER CREATED", run1(ANA));
        assertEquals("USER UPDATED", run1("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", park.users().get(0).getFirstName());
        assertEquals("USER UPDATED", run1("USER UPDATE;ana@correo.com;Clave1!;SURNAME;Garcia"));
        assertEquals("USER UPDATED", run1("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", run1("USER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666"));
        assertEquals("USER UPDATED", run1("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER UPDATED", run1("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertTrue(run1("USER DELETE;ana.nueva@correo.com;Clave1!").startsWith("ERROR: incorrect password"));
        assertEquals("USER DELETED", run1("  USER DELETE;ana.nueva@correo.com;Nueva2#  "));
        assertTrue(park.users().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER CREATE;Ana;Garcia Lopez",
        "USER CREATE;Ana;Garcia Lopez;12345678A;x@correo.com;Clave1!;612345678;1234567890123456",
        "USER CREATE;Ana;Garcia Lopez;11111111H;bad;Clave1!;612345678;1234567890123456",
        "USER CREATE;Ana;Garcia Lopez;11111111H;x@correo.com;clave1!;612345678;1234567890123456",
        "USER CREATE;Ana;Garcia Lopez;11111111H;x@correo.com;Clave1!;612345678;1234",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;x@correo.com;Clave1!;612345678;1234567890123456",
        "USER CREATE;Ana;Garcia Lopez;11111111H;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;ana@correo.com",
        "USER DELETE;nadie@correo.com;Clave1!",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "USER UPDATE;ana@correo.com;Wrong1!;NAME;X",
        "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es",
        "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!",
        "USER UPDATE;ana@correo.com;Clave1!;CARD;1234",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "FOO BAR", "HELP;extra", "EXIT;now", "HELP EXTENDED;x",
        "SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027", "SYSTEM DATE",
        "SYSTEM PRICE;0", "SYSTEM PRICE;-5", "SYSTEM PRICE;abc", "SYSTEM PRICE;10.999",
        "GROUP CREATE;G01", "GROUP CREATE; ;Name", "GROUP ADD;G99;ana@correo.com", "GROUP REMOVE;G99;ana@correo.com",
        "GROUP DELETE;G99",
        "TICKET CREATE;T1;nadie@correo.com;2027-04-15", "TICKET CREATE;T1;ana@correo.com;2027-04-14",
        "TICKET CREATE;T1;ana@correo.com;tomorrow", "TICKET CREATE; ;ana@correo.com;2027-04-15", "TICKET DELETE;T9",
        "GROUPTICKET CREATE;TG;G99;2027-04-15", "GROUPTICKET DELETE;TG",
        "ATTRACTION CREATE;A01;Noria;0", "ATTRACTION CREATE;A01;Noria;abc", "ATTRACTION CREATE;A01;Noria;5;VIP",
        "ATTRACTION CREATE;A01;;5", "ATTRACTION ENTER;A09;T1", "ATTRACTION ENTER;A09;T1;FAST",
        "ATTRACTION EXIT;A09;ana@correo.com", "ATTRACTION LIST;A09", "ATTRACTION STATUS;A09", "ATTRACTION STATUS",
        "SPEEDPASS CREATE;SP;T9;1", "SPEEDPASS CREATE;SP;T9;x", "SPEEDPASS DELETE;SP:1",
        "SPECIALPASS CREATE;PE;T9", "SPECIALPASS DELETE;PE",
        "INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99", "INVOICE CREATE;OTHER;x"
    })
    void rejectedCommandsReportErrorAndChangeNothing(String line) {
        run1(ANA);
        CommandResult result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
        assertFalse(result.exit());
        assertEquals(1, park.users().size());
        assertEquals("Ana", park.users().get(0).getFirstName());
        assertEquals(TestSupport.TODAY, park.getCurrentDate());
        assertEquals(Park.INITIAL_PRICE, park.getBasePrice());
        assertTrue(park.tickets().isEmpty() && park.attractions().isEmpty() && park.groups().isEmpty()
                && park.passes().isEmpty());
    }

    @Test
    void systemCommands() {
        assertEquals("SYSTEM DATE SET TO 2027-05-01", run1("SYSTEM DATE;2027-05-01"));
        assertEquals("SYSTEM PRICE SET TO 45.50 EUR", run1("SYSTEM PRICE;45.50"));
        run1(ANA);
        assertEquals("TICKET CREATED; PRICE: 45.50 EUR", run1("TICKET CREATE;T001;ana@correo.com;2027-05-01"));
    }

    @Test
    void groupAndTicketFlow() {
        run(processor, ANA, PEDRO);
        assertEquals(List.of("GROUP CREATED", "USER ADDED TO GROUP", "USER ADDED TO GROUP"),
                run(processor, "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com",
                        "GROUP ADD;G01;pedro@correo.com"));
        assertTrue(run1("GROUP ADD;G01;ana@correo.com").startsWith("ERROR:"));
        assertEquals("USER REMOVED FROM GROUP", run1("GROUP REMOVE;G01;pedro@correo.com"));
        assertTrue(run1("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR:"));
        run1("GROUP ADD;G01;pedro@correo.com");
        assertEquals("GROUP TICKET CREATED; MEMBERS: 2; TOTAL PRICE: 80.00 EUR",
                run1("GROUPTICKET CREATE;TG01;G01;2027-04-16"));
        assertTrue(run1("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR:"));
        assertTrue(run1("GROUP DELETE;G01").startsWith("ERROR:"));
        assertTrue(run1("USER DELETE;pedro@correo.com;Otra2#").startsWith("ERROR:"));
        assertTrue(run1("TICKET DELETE;TG01:87654321X").startsWith("ERROR:"));
        assertTrue(run1("TICKET DELETE;TG01").startsWith("ERROR:"));
        assertTrue(run1("TICKET CREATE;TG01:87654321X;pedro@correo.com;2027-04-15").startsWith("ERROR:"));
        assertEquals("GROUP TICKET DELETED", run1("GROUPTICKET DELETE;TG01"));
        assertEquals("GROUP DELETED", run1("GROUP DELETE;G01"));
        assertEquals("TICKET CREATED; PRICE: 40.00 EUR", run1("TICKET CREATE;T2;ana@correo.com;2027-04-20"));
        assertEquals("TICKET DELETED", run1("TICKET DELETE;T2"));
        run1("TICKET CREATE;T3;ana@correo.com;2027-04-15");
        assertTrue(run1("TICKET DELETE;T3").startsWith("ERROR:"));
        assertTrue(run1("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR: user has active tickets"));
        run1("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", run1("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void historicalGroupTicketAfterGroupDeletion() {
        run(processor, ANA, "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com", "GROUP CREATE;G02;Empty",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(run1("GROUPTICKET CREATE;TGX;G02;2027-04-15").startsWith("ERROR:"));
        assertTrue(run1("GROUPTICKET CREATE;TGX;G01;2027-04-14").startsWith("ERROR:"));
        assertTrue(run1("GROUPTICKET DELETE;TG01").startsWith("ERROR:"));
        run1("SYSTEM DATE;2027-04-16");
        assertEquals("GROUP DELETED", run1("GROUP DELETE;G01"));
        assertTrue(park.findTicket("TG01").isPresent());
    }

    @Test
    void discountBrackets() {
        int[][] cases = {{2, 80}, {3, 108}, {6, 192}, {11, 220}};
        int next = 1;
        for (int[] c : cases) {
            String group = "G" + c[0];
            run1("GROUP CREATE;" + group + ";Big");
            for (int i = 0; i < c[0]; i++, next++) {
                run1(TestSupport.user(next));
                run1("GROUP ADD;" + group + ";u" + next + "@correo.com");
            }
            assertEquals("GROUP TICKET CREATED; MEMBERS: " + c[0] + "; TOTAL PRICE: " + c[1] + ".00 EUR",
                    run1("GROUPTICKET CREATE;T" + group + ";" + group + ";2027-04-15"));
        }
    }

    @Test
    void attractionFlowWithTypes() {
        run(processor, ANA, PEDRO, LUIS, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "TICKET CREATE;T1;luis@correo.com;2027-04-15", "TICKET CREATE;T2;luis@correo.com;2027-04-16");
        assertEquals(List.of("ATTRACTION CREATED", "ATTRACTION CREATED", "ATTRACTION CREATED", "ATTRACTION CREATED"),
                run(processor, "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATE;A02;Solo;12;INDIVIDUAL",
                        "ATTRACTION CREATE;A03;Rafting;24;GROUP", "ATTRACTION CREATE;A04;Terror;1;SPECIAL"));
        assertTrue(run1("ATTRACTION CREATE;A01;Dup;3").startsWith("ERROR:"));
        assertEquals("NO USERS IN A01", run1("ATTRACTION LIST;A01"));
        assertTrue(run1("ATTRACTION ENTER;A02;TG01").startsWith("ERROR:"));
        assertTrue(run1("ATTRACTION ENTER;A03;T1").startsWith("ERROR:"));
        assertTrue(run1("ATTRACTION ENTER;A04;TG01").startsWith("ERROR:"));
        assertTrue(run1("ATTRACTION ENTER;A01;T2").startsWith("ERROR:"));
        assertEquals("ENTERED A01: ana@correo.com, pedro@correo.com", run1("ATTRACTION ENTER;A01;TG01"));
        assertTrue(run1("ATTRACTION ENTER;A01;TG01").startsWith("ERROR:"));
        assertEquals("ENTERED A03: ana@correo.com, pedro@correo.com", run1("ATTRACTION ENTER;A03;TG01"));
        assertEquals("ENTERED A02: luis@correo.com", run1("ATTRACTION ENTER;A02;T1"));
        assertEquals("ENTERED A02: ana@correo.com", run1("ATTRACTION ENTER;A02;TG01:12345678Z"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), run1("ATTRACTION LIST;A01").lines().toList());
        assertEquals("USER LEFT A01", run1("ATTRACTION EXIT;A01;ana@correo.com"));
        assertTrue(run1("ATTRACTION EXIT;A01;ana@correo.com").startsWith("ERROR:"));
        assertEquals(List.of("ATTRACTION: A01", "NAME: Dragon Rojo", "TYPE: GENERAL", "CAPACITY: 20", "INSIDE: 1",
                "AVAILABLE: 19", "USERS: pedro@correo.com"), run1("ATTRACTION STATUS;A01").lines().toList());
        run1("ATTRACTION EXIT;A01;pedro@correo.com");
        assertTrue(run1("ATTRACTION STATUS;A01").endsWith("USERS: -"));
        run(processor, "ATTRACTION CREATE;A05;Tiny;1", "TICKET CREATE;T3;ana@correo.com;2027-04-15");
        run1("ATTRACTION ENTER;A05;T1");
        assertTrue(run1("ATTRACTION ENTER;A05;T3").startsWith("ERROR: not enough places"));
        run1("SYSTEM DATE;2027-04-20");
        assertEquals("luis@correo.com", run1("ATTRACTION LIST;A05"));
    }

    @Test
    void speedPassesAndSpecialPasses() {
        run(processor, ANA, PEDRO, LUIS, "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUP ADD;G01;luis@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "TICKET CREATE;T002;ana@correo.com;2027-04-20", "SYSTEM PRICE;50",
                "ATTRACTION CREATE;A01;General;20", "ATTRACTION CREATE;A03;Group;20;GROUP",
                "ATTRACTION CREATE;A04;Special;10;SPECIAL");
        assertEquals("SPEEDPASS CREATED: SP01:1; TOTAL PRICE: 8.00 EUR", run1("SPEEDPASS CREATE;SP01;T001;1"));
        assertEquals("SPEEDPASS CREATED: SP02:1 .. SP02:10; TOTAL PRICE: 80.00 EUR",
                run1("SPEEDPASS CREATE;SP02;T002;10"));
        assertTrue(run1("SPEEDPASS CREATE;SP01;T002;10").startsWith("ERROR:"));
        assertEquals(11, park.passes().size());
        assertTrue(run1("SPEEDPASS CREATE;SP03;T001;2").startsWith("ERROR:"));
        assertTrue(run1("SPEEDPASS CREATE;SP03;TG01;1").startsWith("ERROR:"));
        assertEquals("SPECIALPASS CREATED; PRICE: 36.00 EUR", run1("SPECIALPASS CREATE;PE01;TG01:12345678Z"));
        assertTrue(run1("SPECIALPASS CREATE;PE02;TG01:12345678Z").startsWith("ERROR:"));
        assertEquals("SPECIALPASS CREATED; PRICE: 40.00 EUR", run1("SPECIALPASS CREATE;PE03;T001"));
        assertTrue(run1("SPECIALPASS CREATE;PE03;T002").startsWith("ERROR:"));
        // refunds
        assertTrue(run1("SPEEDPASS DELETE;SP01:1").startsWith("ERROR:"));
        assertTrue(run1("SPECIALPASS DELETE;SP02:3").startsWith("ERROR:"));
        assertEquals("SPEEDPASS DELETED", run1("SPEEDPASS DELETE;SP02:3"));
        assertTrue(park.findPass("SP02:4").isPresent());
        // SPEED entry: individual
        assertEquals("ENTERED A01: ana@correo.com; SPEEDPASS CONSUMED: SP01:1", run1("ATTRACTION ENTER;A01;T001;SPEED"));
        run1("ATTRACTION EXIT;A01;ana@correo.com");
        assertTrue(run1("ATTRACTION ENTER;A01;T001;SPEED").startsWith("ERROR:"));
        assertTrue(run1("SPEEDPASS DELETE;SP01:1").startsWith("ERROR:"));
        // SPEED entry: group with one member missing a pass is all-or-nothing
        run(processor, "SPEEDPASS CREATE;SG1;TG01:12345678Z;1", "SPEEDPASS CREATE;SG2;TG01:87654321X;1");
        assertTrue(run1("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ERROR:"));
        assertTrue(park.findAttraction("A03").orElseThrow().getOccupants().isEmpty());
        assertTrue(park.passes().stream().filter(p -> p.getId().startsWith("SG")).noneMatch(p -> p.isConsumed()));
        run1("SPEEDPASS CREATE;SG3;TG01:11111111H;1");
        assertEquals("ENTERED A03: ana@correo.com, pedro@correo.com, luis@correo.com; SPEEDPASS CONSUMED: SG1:1, SG2:1, SG3:1",
                run1("ATTRACTION ENTER;A03;TG01;SPEED"));
        // special attraction
        assertTrue(run1("ATTRACTION ENTER;A04;T001;SPEED").startsWith("ERROR:"));
        assertTrue(run1("ATTRACTION ENTER;A04;TG01:87654321X").startsWith("ERROR:"));
        assertEquals("ENTERED A04: ana@correo.com; SPECIAL PASS CONSUMED: PE03", run1("ATTRACTION ENTER;A04;T001"));
        run1("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(run1("ATTRACTION ENTER;A04;T001").startsWith("ERROR:"));
        assertTrue(run1("SPECIALPASS CREATE;PE09;T001").startsWith("ERROR:"));
        // cascade
        assertEquals("TICKET DELETED", run1("TICKET DELETE;T002"));
        assertTrue(park.passes().stream().noneMatch(p -> p.getId().startsWith("SP02")));
    }

    @Test
    void groupTicketDeletionCascadesToPasses() {
        run(processor, ANA, PEDRO, "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUPTICKET CREATE;TG01;G01;2027-04-18",
                "SPEEDPASS CREATE;S1;TG01:12345678Z;10", "SPECIALPASS CREATE;P1;TG01:87654321X",
                "TICKET CREATE;T1;ana@correo.com;2027-04-18", "SPEEDPASS CREATE;S2;T1;1");
        assertEquals(12, park.passes().size());
        assertEquals("GROUP TICKET DELETED", run1("GROUPTICKET DELETE;TG01"));
        assertEquals(List.of("S2:1"), park.passes().stream().map(p -> p.getId()).toList());
        assertTrue(park.findTicket("TG01:12345678Z").isEmpty());
    }

    @Test
    void invoices() {
        run(processor, ANA, PEDRO, LUIS, "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com", "GROUP ADD;G01;luis@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SPEEDPASS CREATE;SP01;T001;1", "SPECIALPASS CREATE;PE01;TG01:12345678Z",
                "SPEEDPASS CREATE;SP09;TG01:87654321X;1", "GROUP CREATE;G02;Empty");
        String user = run1("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(user.contains("INVOICE - USER") && user.contains("DNI: 12345678Z"));
        assertTrue(user.contains("[TICKETS]") && user.contains("Ticket T001 (2027-04-15): 40.00 EUR"));
        assertTrue(user.contains("SpeedPass SP01:1 for ticket T001: 8.00 EUR"));
        assertTrue(user.contains("Special Pass PE01 for ticket TG01:12345678Z: 36.00 EUR"));
        assertFalse(user.contains("Ticket TG01"));
        assertTrue(user.endsWith("TOTAL: 84.00 EUR"));
        String pedro = run1("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(pedro.contains("[TICKETS]") || pedro.contains("[SPECIAL PASSES]"));
        assertTrue(pedro.endsWith("TOTAL: 7.20 EUR"));
        String group = run1("INVOICE CREATE;GROUP;G01");
        assertTrue(group.contains("Group: G01") && group.contains("Name: Familia"));
        assertTrue(group.contains("Group ticket TG01 (2027-04-15, 3 members): 108.00 EUR"));
        assertTrue(group.contains("- TG01:12345678Z ana@correo.com (36.00 EUR, included)"));
        assertTrue(group.endsWith("TOTAL: 108.00 EUR"));
        assertEquals(List.of("INVOICE - GROUP", "Group: G02", "Name: Empty", "TOTAL: 0.00 EUR"),
                run1("INVOICE CREATE;GROUP;G02").lines().toList());
    }

    @Test
    void helpAndExtendedHelp() {
        String help = run1("HELP");
        String extended = run1("HELP EXTENDED");
        for (String syntax : List.of(
                "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                "USER DELETE;<email>;<password>", "USER UPDATE;<email>;<password>;<field>;<value>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
                "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>",
                "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "ATTRACTION ENTER;<attraction>;<ticket>", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>",
                "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP", "HELP EXTENDED", "EXIT")) {
            assertTrue(help.contains(syntax), syntax);
            assertTrue(extended.contains(syntax), syntax);
        }
        assertFalse(help.contains("Registers a user"));
        assertTrue(extended.contains("Registers a user"));
        assertTrue(processor.process("EXIT").exit());
    }
}
