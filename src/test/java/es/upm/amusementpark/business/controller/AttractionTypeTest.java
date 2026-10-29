package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.attraction.AttractionType;
import es.upm.amusementpark.business.model.attraction.EntryResult;
import es.upm.amusementpark.business.model.attraction.GeneralAttraction;
import es.upm.amusementpark.business.model.attraction.GroupAttraction;
import es.upm.amusementpark.business.model.attraction.IndividualAttraction;
import es.upm.amusementpark.business.model.attraction.SpecialAttraction;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.ticket.AccessTicket;
import es.upm.amusementpark.business.model.ticket.GroupAccess;
import es.upm.amusementpark.business.model.ticket.GroupTicket;
import es.upm.amusementpark.business.model.ticket.IndividualAccess;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Typed attractions, generic access restrictions and pass consumption (Delivery 3, F3-C07, F3-C08).
 */
class AttractionTypeTest extends ParkTestSupport {

    @BeforeEach
    void park() throws Exception {
        createAna();
        createPedro();
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createAttraction("A02", "Montana Rusa Individual", "12", "INDIVIDUAL");
        park.createAttraction("A03", "Rafting Familiar", "24", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
    }

    @Test
    void createsTypedAttractions() throws Exception {
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A03").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertThrows(ValidationException.class, () -> park.createAttraction("A05", "X", "10", "VIP"));
        assertThrows(ValidationException.class, () -> park.createAttraction("A05", "X", "10", "general"));
    }

    @Test
    void genericBoundsMatchAccessCapabilities() {
        assertEquals(AccessTicket.class, typeArgument(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAccess.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertFalse(GroupAccess.class.isAssignableFrom(IndividualTicket.class));
        assertFalse(IndividualAccess.class.isAssignableFrom(GroupTicket.class));
    }

    private static Class<?> typeArgument(Class<?> attractionClass) {
        ParameterizedType superType = (ParameterizedType) attractionClass.getGenericSuperclass();
        assertEquals(Attraction.class, superType.getRawType());
        return (Class<?>) superType.getActualTypeArguments()[0];
    }

    @Test
    void ticketKindsAcceptedByType() throws Exception {
        assertEquals(1, park.enterAttraction("A01", "T001"));
        park.exitAttraction("A01", "ana@correo.com");
        assertEquals(2, park.enterAttraction("A01", "TG01"));
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A02", "TG01"));
        assertEquals(1, park.enterAttraction("A02", "T001"));
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A03", "T001"));
        assertEquals(2, park.enterAttraction("A03", "TG01"));
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A04", "TG01"));
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void individualSpeedEntryConsumesOnePass() throws Exception {
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpeedPasses("SP02", "T001", "10");
        EntryResult result = park.enterAttraction("A02", "T001", true);
        assertEquals(1, result.admitted());
        assertEquals(List.of(park.findPass("SP01:1")), result.consumedPasses());
        assertEquals(1, park.getPasses().stream().filter(Pass::isConsumed).count());
    }

    @Test
    void speedWithoutPassRejected() throws Exception {
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A01", "T001", true));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void groupSpeedEntryIsAtomic() throws Exception {
        park.buySpeedPasses("SP01", "TG01:12345678Z", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A03", "TG01", true));
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertFalse(park.findPass("SP01:1").isConsumed());

        park.buySpeedPasses("SP02", "TG01:87654321X", "1");
        park.createAttraction("A05", "Rafting Pequeno", "1", "GROUP");
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A05", "TG01", true));
        assertFalse(park.findPass("SP01:1").isConsumed());
        assertFalse(park.findPass("SP02:1").isConsumed());

        EntryResult result = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, result.admitted());
        assertTrue(park.findPass("SP01:1").isConsumed());
        assertTrue(park.findPass("SP02:1").isConsumed());
    }

    @Test
    void groupSpeedAtGeneralAttraction() throws Exception {
        park.buySpeedPasses("SP01", "TG01:12345678Z", "1");
        park.buySpeedPasses("SP02", "TG01:87654321X", "1");
        assertEquals(2, park.enterAttraction("A01", "TG01", true).consumedPasses().size());
    }

    @Test
    void specialEntryConsumesSpecialPass() throws Exception {
        park.buySpecialPass("PE01", "T001");
        EntryResult result = park.enterAttraction("A04", "T001", false);
        assertEquals(List.of(park.findPass("PE01")), result.consumedPasses());
        assertTrue(park.findPass("PE01").isConsumed());
        assertTrue(park.findAttraction("A04").contains(park.findUser("ana@correo.com")));
    }

    @Test
    void specialRejections() throws Exception {
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A04", "T001", false));
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A04", "T001", true));
        assertFalse(park.findPass("PE01").isConsumed());
        assertFalse(park.findPass("SP01:1").isConsumed());

        park.enterAttraction("A04", "T001", false);
        park.exitAttraction("A04", "ana@correo.com");
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A04", "T001", false));
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void failedSpecialEntryDoesNotConsumePass() throws Exception {
        park.createAttraction("A06", "Mini Terror", "1", "SPECIAL");
        park.createTicket("T002", "pedro@correo.com", TODAY_TEXT);
        park.buySpecialPass("PE02", "T002");
        park.buySpecialPass("PE01", "T001");
        park.enterAttraction("A06", "T002", false);
        assertThrows(OperationNotAllowedException.class, () -> park.enterAttraction("A06", "T001", false));
        assertFalse(park.findPass("PE01").isConsumed());
    }
}
