package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.AttractionOperationException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static User user(String email) {
        return new User("Ana", "Garcia Lopez", "12345678Z", email, "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void newAttractionIsEmpty() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
        assertEquals(20, attraction.getAvailablePlaces());
    }

    @Test
    void occupantsAreKeptByTheAttraction() {
        Attraction<?> attraction = new GeneralAttraction("A01", "Dragon Rojo", 20);
        User ana = user("ana@correo.com");
        attraction.enter(List.of(ana));
        assertTrue(attraction.isInside(ana));
        assertEquals(19, attraction.getAvailablePlaces());
        assertThrows(UnsupportedOperationException.class, () -> attraction.getOccupants().clear());
        assertTrue(attraction.exit(ana));
        assertFalse(attraction.exit(ana));
    }

    @Test
    void usersDoNotReferenceAttractions() {
        assertTrue(Arrays.stream(User.class.getDeclaredFields())
                .noneMatch(f -> f.getType() == Attraction.class || f.getType().isAssignableFrom(List.class)));
    }

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private static IndividualTicket individual(User user) {
        return new IndividualTicket("T001", DAY, user, new BigDecimal("40.00"));
    }

    private static GroupTicket group(User a, User b) {
        return new GroupTicket("TG01", DAY, new Group("G01", "F"), List.of(
                new IndividualTicket("TG01:1", DAY, a, new BigDecimal("40.00")),
                new IndividualTicket("TG01:2", DAY, b, new BigDecimal("40.00"))));
    }

    private static final PassLookup NO_PASSES = ticket -> List.of();

    @Test
    void eachTypeAcceptsItsTickets() throws Exception {
        User ana = user("ana@correo.com");
        User pedro = user("pedro@correo.com");
        IndividualTicket t = individual(ana);
        GroupTicket g = group(ana, pedro);
        assertEquals(t, new GeneralAttraction("A01", "G", 20).accept(t, NO_PASSES));
        assertEquals(g, new GeneralAttraction("A01", "G", 20).accept(g, NO_PASSES));
        assertEquals(t, new IndividualAttraction("A02", "I", 12).accept(t, NO_PASSES));
        assertThrows(AttractionOperationException.class, () -> new IndividualAttraction("A02", "I", 12).accept(g, NO_PASSES));
        assertEquals(g, new GroupAttraction("A03", "G", 24).accept(g, NO_PASSES));
        assertThrows(AttractionOperationException.class, () -> new GroupAttraction("A03", "G", 24).accept(t, NO_PASSES));
        assertThrows(AttractionOperationException.class, () -> new SpecialAttraction("A04", "S", 15).accept(g, NO_PASSES));
    }

    @Test
    void specialAttractionRequiresAnUnconsumedSpecialPass() throws Exception {
        IndividualTicket t = individual(user("ana@correo.com"));
        SpecialAttraction special = new SpecialAttraction("A04", "Pasaje del Terror", 15);
        assertThrows(AttractionOperationException.class, () -> special.accept(t, NO_PASSES));
        SpecialPass pass = new SpecialPass("PE01", t);
        PassLookup withPass = ticket -> List.of(pass);
        assertEquals(t, special.accept(t, withPass));
        pass.consume();
        assertThrows(AttractionOperationException.class, () -> special.accept(t, withPass));
        assertThrows(AttractionOperationException.class,
                () -> special.accept(t, ticket -> List.of(new SpeedPass("SP01:1", t))));
    }

    @Test
    void typedAdmitLetsTheWholeTicketInOrNobody() throws Exception {
        GroupAttraction small = new GroupAttraction("A03", "G", 1);
        GroupTicket g = group(user("ana@correo.com"), user("pedro@correo.com"));
        assertThrows(AttractionOperationException.class, () -> small.admit(g));
        assertTrue(small.getOccupants().isEmpty());
        GroupAttraction big = new GroupAttraction("A05", "G", 5);
        assertEquals(2, big.admit(g).size());
        assertEquals(3, big.getAvailablePlaces());
    }

    @Test
    void attractionTypesBindBoundedTypeParameters() {
        TypeVariable<?> parameter = Attraction.class.getTypeParameters()[0];
        assertEquals(AccessTicket.class, parameter.getBounds()[0]);
        assertEquals(AccessTicket.class, typeArgument(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAccess.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAccess.class, typeArgument(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertFalse(IndividualAccess.class.isAssignableFrom(GroupTicket.class));
    }

    private static Type typeArgument(Class<?> attraction) {
        return ((ParameterizedType) attraction.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Test
    void typesCreateTheirAttractions() {
        for (AttractionType type : AttractionType.values()) {
            assertEquals(type, type.create("A", "N", 1).getType());
        }
        assertEquals(null, AttractionType.fromName("VIP"));
    }
}
