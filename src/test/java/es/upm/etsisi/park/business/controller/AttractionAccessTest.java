package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.exception.AccessDeniedException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.AttractionType;
import es.upm.etsisi.park.business.model.EntryResult;
import es.upm.etsisi.park.business.model.GeneralAttraction;
import es.upm.etsisi.park.business.model.GroupAttraction;
import es.upm.etsisi.park.business.model.IndividualAttraction;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.SpecialAttraction;
import es.upm.etsisi.park.business.model.SpeedPass;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionAccessTest extends Delivery3ScenarioBase {

    @Test
    void singleton() {
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
    }

    @Test
    void typesCreateMatchingClasses() {
        assertInstanceOf(GeneralAttraction.class, park.requireAttraction("A01"));
        assertInstanceOf(IndividualAttraction.class, park.requireAttraction("A02"));
        assertInstanceOf(GroupAttraction.class, park.requireAttraction("A03"));
        assertInstanceOf(SpecialAttraction.class, park.requireAttraction("A04"));
        assertEquals(AttractionType.GENERAL, park.createAttraction("A05", "Noria", "10", null).getType());
        assertEquals(AttractionType.SPECIAL, park.createAttraction("A06", "Show", "10", "special").getType());
        assertThrows(ValidationException.class, () -> park.createAttraction("A07", "Noria", "10", "VIP"));
    }

    @Test
    void acceptedTicketKindsPerType() {
        Attraction<?> general = park.requireAttraction("A01");
        Attraction<?> individual = park.requireAttraction("A02");
        Attraction<?> group = park.requireAttraction("A03");
        Attraction<?> special = park.requireAttraction("A04");
        var t001 = park.findTicket("T001").orElseThrow();
        var tg01 = park.findTicket("TG01").orElseThrow();
        var component = park.findTicket("TG01:12345678Z").orElseThrow();
        assertTrue(general.accepts(t001) && general.accepts(tg01));
        assertTrue(individual.accepts(t001) && individual.accepts(component) && !individual.accepts(tg01));
        assertTrue(group.accepts(tg01) && !group.accepts(t001));
        assertTrue(special.accepts(t001) && !special.accepts(tg01));
    }

    @Test
    void wrongTicketKindIsDenied() {
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A02", "TG01", false));
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A03", "T001", false));
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A04", "TG01", false));
        assertEquals(0, park.requireAttraction("A02").getOccupancy());
    }

    @Test
    void normalEntryDoesNotConsumeSpeedPasses() {
        List<SpeedPass> passes = park.buySpeedPasses("SP01", "T001", "10");
        EntryResult result = park.enterAttraction("A01", "T001", false);
        assertTrue(result.consumed().isEmpty());
        assertTrue(passes.stream().noneMatch(Pass::isConsumed));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        List<SpeedPass> passes = park.buySpeedPasses("SP01", "T001", "10");
        EntryResult result = park.enterAttraction("A02", "T001", true);
        assertEquals(1, result.consumed().size());
        assertEquals(1, passes.stream().filter(Pass::isConsumed).count());
        assertTrue(result.consumed().get(0).isConsumed());
    }

    @Test
    void speedEntryWithoutPassIsDenied() {
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A02", "T001", true));
        assertFalse(park.requireAttraction("A02").isInside(park.users().findByEmail("ana@correo.com").orElseThrow()));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        park.buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.buySpeedPasses("SP03", "TG01:87654321X", "1");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A01", "TG01", true));
        assertEquals(0, park.requireAttraction("A01").getOccupancy());
        assertFalse(park.findPass("SP02:1").orElseThrow().isConsumed());
        assertFalse(park.findPass("SP03:1").orElseThrow().isConsumed());

        park.buySpeedPasses("SP04", "TG01:11111111H", "1");
        EntryResult result = park.enterAttraction("A03", "TG01", true);
        assertEquals(3, result.entered().size());
        assertEquals(3, result.consumed().size());
        assertTrue(park.findPass("SP02:1").orElseThrow().isConsumed());
        assertTrue(park.findPass("SP04:1").orElseThrow().isConsumed());
    }

    @Test
    void capacityFailureConsumesNothing() {
        park.createAttraction("A09", "Mini", "2", "GROUP");
        park.buySpeedPasses("SP02", "TG01:12345678Z", "1");
        park.buySpeedPasses("SP03", "TG01:87654321X", "1");
        park.buySpeedPasses("SP04", "TG01:11111111H", "1");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A09", "TG01", true));
        assertFalse(park.findPass("SP02:1").orElseThrow().isConsumed());
    }

    @Test
    void specialEntryConsumesSpecialPassOnce() {
        park.buySpecialPass("PE01", "T001");
        EntryResult result = park.enterAttraction("A04", "T001", false);
        assertEquals(List.of("PE01"), result.consumed().stream().map(Pass::getId).toList());
        assertTrue(park.findPass("PE01").orElseThrow().isConsumed());
        park.exitAttraction("A04", "ana@correo.com");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A04", "T001", false));
    }

    @Test
    void specialAttractionRejectsSpeed() {
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A04", "T001", true));
        assertFalse(park.findPass("PE01").orElseThrow().isConsumed());
        assertFalse(park.findPass("SP01:1").orElseThrow().isConsumed());
    }

    @Test
    void ticketDateStillChecked() {
        park.createTicket("T002", "pedro@correo.com", FUTURE);
        park.buySpeedPasses("SP09", "T002", "1");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A02", "T002", true));
        assertFalse(park.findPass("SP09:1").orElseThrow().isConsumed());
    }
}
