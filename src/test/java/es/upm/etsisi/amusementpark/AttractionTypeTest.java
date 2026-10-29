package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.GeneralAccess;
import es.upm.etsisi.amusementpark.business.model.GroupAccess;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualAccess;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.model.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.model.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.model.attraction.GeneralAttraction;
import es.upm.etsisi.amusementpark.business.model.attraction.GroupAttraction;
import es.upm.etsisi.amusementpark.business.model.attraction.IndividualAttraction;
import es.upm.etsisi.amusementpark.business.model.attraction.SpecialAttraction;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionTypeTest extends ParkFixture {

    PassController passes;

    @BeforeEach
    void setUpPark() throws Exception {
        passes = PassController.getInstance();
        park.createAttraction("A01", "Dragon Rojo", "20", "GENERAL");
        park.createAttraction("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        park.createAttraction("A03", "Rafting Familiar", "24", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createAttraction("A05", "Mini", "1", "GENERAL");
        park.createTicket("T001", ANA, "2027-04-15");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", ANA);
        park.addToGroup("G01", PEDRO);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
    }

    private Pass pass(String id) {
        return passes.findPass(id);
    }

    @Test
    void typesAndGenericBounds() {
        assertInstanceOf(GeneralAttraction.class, park.findAttraction("A01"));
        assertInstanceOf(IndividualAttraction.class, park.findAttraction("A02"));
        assertInstanceOf(GroupAttraction.class, park.findAttraction("A03"));
        assertInstanceOf(SpecialAttraction.class, park.findAttraction("A04"));
        TypeVariable<?> t = Attraction.class.getTypeParameters()[0];
        assertEquals(GeneralAccess.class, t.getBounds()[0]);
        assertEquals(GeneralAccess.class, typeArgument(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAccess.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertFalse(GroupAccess.class.isAssignableFrom(IndividualTicket.class));
    }

    private static Object typeArgument(Class<?> type) {
        return ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Test
    void creationDefaultsAndValidation() throws Exception {
        park.createAttraction("A06", "Noria", "10");
        assertEquals(AttractionType.GENERAL, park.findAttraction("A06").getType());
        assertThrows(ValidationException.class, () -> park.createAttraction("A07", "Noria", "10", "VIP"));
        CommandProcessor processor = processor();
        assertEquals("ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A08;Casa;5;SPECIAL"));
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A08").getType());
        assertTrue(processor.process("ATTRACTION STATUS;A08").contains("TYPE: SPECIAL"));
    }

    @Test
    void accessRestrictionsByType() throws Exception {
        assertEquals(1, park.enterAttraction("A01", "T001", false).users());
        park.exitAttraction("A01", ANA);
        assertEquals(2, park.enterAttraction("A01", "TG01", false).users());
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A02", "TG01", false));
        assertEquals(1, park.enterAttraction("A02", "T001", false).users());
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A03", "T001", false));
        assertEquals(2, park.enterAttraction("A03", "TG01", false).users());
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A04", "TG01", false));
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() throws Exception {
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpeedPasses("SP02", "T001", "1");
        EntryResult result = park.enterAttraction("A02", "T001", true);
        assertEquals(List.of(pass("SP01:1")), result.consumedPasses());
        assertTrue(pass("SP01:1").isConsumed());
        assertFalse(pass("SP02:1").isConsumed());
        String out = processor().process("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals("ATTRACTION ENTERED: 1 user(s). SPEEDPASS CONSUMED: SP02:1", out);
    }

    @Test
    void speedEntryWithoutPassOrFailingChecksChangesNothing() throws Exception {
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A01", "T001", true));
        park.buySpeedPasses("SP01", "T001", "1");
        park.createTicket("T002", PEDRO, "2027-04-15");
        park.enterAttraction("A05", "T002", false);
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A05", "T001", true));
        assertFalse(pass("SP01:1").isConsumed());
        assertTrue(processor().process("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR:"));
        assertFalse(pass("SP01:1").isConsumed());
    }

    @Test
    void groupSpeedEntryIsAtomic() throws Exception {
        park.buySpeedPasses("SP10", "TG01:12345678Z", "1");
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A03", "TG01", true));
        assertFalse(pass("SP10:1").isConsumed());
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());

        park.buySpeedPasses("SP11", "TG01:87654321X", "1");
        EntryResult result = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, result.users());
        assertTrue(pass("SP10:1").isConsumed());
        assertTrue(pass("SP11:1").isConsumed());
    }

    @Test
    void groupSpeedEntryRejectedByCapacityKeepsPasses() throws Exception {
        park.buySpeedPasses("SP10", "TG01:12345678Z", "1");
        park.buySpeedPasses("SP11", "TG01:87654321X", "1");
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A05", "TG01", true));
        assertFalse(pass("SP10:1").isConsumed());
        assertFalse(pass("SP11:1").isConsumed());
    }

    @Test
    void specialEntryConsumesSpecialPass() throws Exception {
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A04", "T001", false));
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A04", "T001", true));
        assertFalse(pass("PE01").isConsumed());
        assertFalse(pass("SP01:1").isConsumed());

        assertEquals("ATTRACTION ENTERED: 1 user(s). SPECIAL PASS CONSUMED: PE01",
                processor().process("ATTRACTION ENTER;A04;T001"));
        assertTrue(pass("PE01").isConsumed());
        park.exitAttraction("A04", ANA);
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A04", "T001", false));
    }

    @Test
    void compositionTicketWithSpecialPassEntersSpecial() throws Exception {
        park.buySpecialPass("PE02", "TG01:87654321X");
        assertEquals(1, park.enterAttraction("A04", "TG01:87654321X", false).users());
        assertTrue(pass("PE02").isConsumed());
    }

    @Test
    void specialEntryWrongDateDoesNotConsume() throws Exception {
        park.buySpecialPass("PE01", "T001");
        park.setCurrentDate("2027-04-14");
        assertThrows(ForbiddenOperationException.class, () -> park.enterAttraction("A04", "T001", false));
        assertFalse(pass("PE01").isConsumed());
    }
}
