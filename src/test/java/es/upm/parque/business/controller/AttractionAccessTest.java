package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.dni;
import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GeneralAttraction;
import es.upm.parque.business.model.GroupAttraction;
import es.upm.parque.business.model.IndividualAttraction;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialAttraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AttractionAccessTest {

    @TempDir
    Path dir;

    private TestContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ctx.group("G01", 3);
        ctx.user(9);
        ok(ctx.tickets().createTicket("T001", email(9), "2026-10-15"));
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(ctx.run("ATTRACTION CREATE;GEN;General;20"));
        ok(ctx.run("ATTRACTION CREATE;IND;Individual;20;INDIVIDUAL"));
        ok(ctx.run("ATTRACTION CREATE;GRP;Grupo;20;GROUP"));
        ok(ctx.run("ATTRACTION CREATE;SPE;Especial;20;SPECIAL"));
        ok(ctx.park().setSystemDate("2026-10-15"));
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) ctx.tickets().find(id);
    }

    private long unusedSpeedPasses(String ticketId) {
        return ticket(ticketId).getSpeedPasses().stream().filter(p -> !p.isConsumed()).count();
    }

    @Test
    void typesAreCreatedAndThePreviousSyntaxMeansGeneral() {
        assertInstanceOf(GeneralAttraction.class, ctx.attractions().find("GEN"));
        assertInstanceOf(IndividualAttraction.class, ctx.attractions().find("IND"));
        assertInstanceOf(GroupAttraction.class, ctx.attractions().find("GRP"));
        assertInstanceOf(SpecialAttraction.class, ctx.attractions().find("SPE"));
        error(ctx.run("ATTRACTION CREATE;VIP;Vip;10;VIP"));
        assertTrue(ctx.run("ATTRACTION STATUS;GRP").contains("Type: GROUP"));
    }

    @Test
    void attractionTypesRestrictTicketKinds() {
        ok(ctx.run("ATTRACTION ENTER;GEN;T001"));
        ok(ctx.run("ATTRACTION ENTER;GEN;TG01"));
        ok(ctx.run("ATTRACTION ENTER;IND;T001"));
        error(ctx.run("ATTRACTION ENTER;IND;TG01"));
        error(ctx.run("ATTRACTION ENTER;GRP;T001"));
        ok(ctx.run("ATTRACTION ENTER;GRP;TG01"));
        ok(ctx.run("ATTRACTION ENTER;IND;TG01-" + dni(1)));
        assertEquals(2, ctx.attractions().find("IND").getPresent().size());
        assertEquals(3, ctx.attractions().find("GRP").getPresent().size());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOneSpeedPass() {
        ok(ctx.run("SPEEDPASS CREATE;T001;10"));
        String answer = ctx.run("ATTRACTION ENTER;IND;T001;SPEED");
        assertEquals("OK ENTERED IND -> 1 user(s); SpeedPass consumed: T001-SP1", answer);
        assertEquals(9, unusedSpeedPasses("T001"));
        assertEquals(10, ticket("T001").getSpeedPasses().size());
        error(ctx.run("ATTRACTION ENTER;GEN;T001;FAST"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        ok(ctx.run("SPEEDPASS CREATE;TG01-" + dni(1) + ";1"));
        ok(ctx.run("SPEEDPASS CREATE;TG01-" + dni(2) + ";1"));
        error(ctx.run("ATTRACTION ENTER;GRP;TG01;SPEED"));
        assertTrue(ctx.attractions().find("GRP").getPresent().isEmpty());
        assertEquals(1, unusedSpeedPasses("TG01-" + dni(1)));
        ok(ctx.run("SPEEDPASS CREATE;TG01-" + dni(3) + ";1"));
        String answer = ctx.run("ATTRACTION ENTER;GEN;TG01;SPEED");
        assertTrue(answer.startsWith("OK ENTERED GEN -> 3 user(s); SpeedPass consumed:"), answer);
        for (int i = 1; i <= 3; i++) {
            assertEquals(0, unusedSpeedPasses("TG01-" + dni(i)));
        }
    }

    @Test
    void failedEntryConsumesNothing() {
        ok(ctx.run("ATTRACTION CREATE;MINI;Mini;2"));
        for (int i = 1; i <= 3; i++) {
            ok(ctx.run("SPEEDPASS CREATE;TG01-" + dni(i) + ";1"));
        }
        error(ctx.run("ATTRACTION ENTER;MINI;TG01;SPEED"));
        for (int i = 1; i <= 3; i++) {
            assertEquals(1, unusedSpeedPasses("TG01-" + dni(i)));
        }
        ok(ctx.park().setSystemDate("2026-10-14"));
        error(ctx.run("ATTRACTION ENTER;GEN;TG01;SPEED"));
        assertEquals(1, unusedSpeedPasses("TG01-" + dni(1)));
    }

    @Test
    void specialAttractionConsumesTheSpecialPassAndRejectsSpeed() {
        error(ctx.run("ATTRACTION ENTER;SPE;T001"));
        ok(ctx.run("SPECIALPASS CREATE;T001"));
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        error(ctx.run("ATTRACTION ENTER;SPE;T001;SPEED"));
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertEquals(1, unusedSpeedPasses("T001"));
        error(ctx.run("ATTRACTION ENTER;SPE;TG01"));
        assertEquals("OK ENTERED SPE -> 1 user(s); Special Pass consumed: T001-SPECIAL",
                ctx.run("ATTRACTION ENTER;SPE;T001"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        ok(ctx.run("ATTRACTION EXIT;SPE;T001"));
        error(ctx.run("ATTRACTION ENTER;SPE;T001"));
    }

    @Test
    void consumedStatusAndTypesArePersisted() throws IOException {
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        ok(ctx.run("ATTRACTION ENTER;GEN;T001;SPEED"));
        TestContext restarted = ctx.restart();
        assertTrue(((IndividualTicket) restarted.tickets().find("T001")).getSpeedPasses().get(0).isConsumed());
        assertEquals(AttractionType.SPECIAL, restarted.attractions().find("SPE").getType());
        assertEquals(AttractionType.GROUP, restarted.attractions().find("GRP").getType());
        assertTrue(restarted.attractions().find("GEN").getPresent().isEmpty());
    }

    @Test
    void attractionsWithoutStoredTypeLoadAsGeneral() throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), "OLD;Antigua;5\n");
        assertEquals(AttractionType.GENERAL, ctx.restart().attractions().find("OLD").getType());
    }
}
