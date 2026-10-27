package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.AttractionOperationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionServiceTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private AttractionService attractions;
    private TicketService tickets;
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() throws Exception {
        UserRepository userRepository = new UserRepository();
        park.initialize(userRepository, new GroupRepository(), DAY.minusDays(14));
        UserService users = UserService.getInstance();
        attractions = AttractionService.getInstance();
        tickets = TicketService.getInstance();
        ana = users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        pedro = users.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        tickets.createTicket("T001", "ana@correo.com", DAY);
        tickets.createTicket("T002", "pedro@correo.com", DAY);
        attractions.createAttraction("A01", "Dragon Rojo", 20);
        park.setCurrentDate(DAY);
    }

    private Attraction a01() {
        return park.getAttractions().findById("A01").orElseThrow();
    }

    @Test
    void validTicketLetsItsUserIn() throws AttractionOperationException {
        attractions.enter("A01", "T001");
        assertTrue(a01().isInside(ana));
        assertEquals(19, a01().getAvailablePlaces());
    }

    @Test
    void ticketForAnotherDateIsRejected() {
        park.setCurrentDate(DAY.minusDays(1));
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A01", "T001"));
        assertFalse(a01().isInside(ana));
    }

    @Test
    void unknownTicketOrAttractionIsRejected() {
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A01", "T999"));
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A99", "T001"));
        assertTrue(a01().getOccupants().isEmpty());
    }

    @Test
    void fullAttractionIsRejected() throws AttractionOperationException {
        attractions.createAttraction("A03", "Pequena", 1);
        attractions.enter("A03", "T002");
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A03", "T001"));
        Attraction a03 = park.getAttractions().findById("A03").orElseThrow();
        assertFalse(a03.isInside(ana));
        assertTrue(a03.isInside(pedro));
    }

    @Test
    void presenceDoesNotDependOnLaterDateChanges() throws AttractionOperationException {
        attractions.enter("A01", "T001");
        park.setCurrentDate(DAY.plusDays(1));
        assertTrue(a01().isInside(ana));
    }

    @Test
    void userAlreadyInsideCannotEnterAgain() throws AttractionOperationException {
        attractions.enter("A01", "T001");
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A01", "T001"));
        assertEquals(1, a01().getOccupants().size());
    }

    private void buyGroupTicket() throws Exception {
        park.setCurrentDate(DAY.minusDays(14));
        GroupService groups = GroupService.getInstance();
        groups.createGroup("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", DAY);
        park.setCurrentDate(DAY);
    }

    @Test
    void groupEntersTogether() throws Exception {
        buyGroupTicket();
        attractions.enter("A01", "TG01");
        assertTrue(a01().isInside(ana) && a01().isInside(pedro));
        assertEquals(18, a01().getAvailablePlaces());
    }

    @Test
    void insufficientCapacityLetsNobodyIn() throws Exception {
        buyGroupTicket();
        attractions.createAttraction("A02", "Torre", 1);
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A02", "TG01"));
        assertTrue(park.getAttractions().findById("A02").orElseThrow().getOccupants().isEmpty());
    }

    @Test
    void groupTicketForAnotherDateIsRejected() throws Exception {
        buyGroupTicket();
        park.setCurrentDate(DAY.minusDays(1));
        assertThrows(AttractionOperationException.class, () -> attractions.enter("A01", "TG01"));
        assertTrue(a01().getOccupants().isEmpty());
    }

    @Test
    void groupMembersLeaveIndividually() throws Exception {
        buyGroupTicket();
        attractions.enter("A01", "TG01");
        attractions.exit("A01", "ana@correo.com");
        assertFalse(a01().isInside(ana));
        assertTrue(a01().isInside(pedro));
        assertEquals(19, a01().getAvailablePlaces());
    }

    @Test
    void exitOfUserNotInsideOrUnknownAttractionIsRejected() {
        assertThrows(AttractionOperationException.class, () -> attractions.exit("A01", "luis@correo.com"));
        assertThrows(AttractionOperationException.class, () -> attractions.exit("A99", "ana@correo.com"));
    }
}
