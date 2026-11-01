package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

import es.upm.amusementpark.business.model.Admission;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAdmission;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.IndividualAdmission;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.SpecialAttraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionTypeCommandsTest extends ParkTestSupport {

    @BeforeEach
    void tickets() {
        createUsers(3);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + email(2));
        ok("GROUP ADD;G01;" + email(3));
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-01");
    }

    @Test
    void e2SyntaxCreatesGeneralAndTypeIsOptional() {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        ok("ATTRACTION CREATE;A02;Casa Magica;5;SPECIAL");
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A02").getType());
        error("ATTRACTION CREATE;A03;X;5;VIP");
        error("ATTRACTION CREATE;A03;X;5;");
        assertNull(park.findAttraction("A03"));
        assertTrue(run("ATTRACTION STATUS;A02").contains("Type: SPECIAL"));
    }

    @ParameterizedTest
    @CsvSource({"GENERAL, true, true", "INDIVIDUAL, true, false", "GROUP, false, true", "SPECIAL, true, false"})
    void typeDecidesAdmittedTicketKinds(String type, boolean individualEnters, boolean groupEnters) {
        ok("ATTRACTION CREATE;A01;Tornado;20;" + type);
        assertEquals(individualEnters, run("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertEquals(groupEnters, run("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        int expected = (individualEnters ? 1 : 0) + (groupEnters ? 2 : 0);
        assertEquals(expected, park.findAttraction("A01").getVisitors().size());
    }

    @Test
    void constituentTicketActsAsIndividualTicket() {
        ok("ATTRACTION CREATE;A01;Solo;5;INDIVIDUAL");
        expect("OK: 1 user(s) entered attraction A01", "ATTRACTION ENTER;A01;TG01-1");
        assertEquals(1, park.findAttraction("A01").getVisitors().size());
        expect("OK: 1 user(s) left attraction A01", "ATTRACTION EXIT;A01;TG01-1");
    }

    @Test
    void genericBoundsRestrictAdmissions() {
        assertEquals(Admission.class, admittedBound(GeneralAttraction.class));
        assertEquals(IndividualAdmission.class, admittedBound(IndividualAttraction.class));
        assertEquals(GroupAdmission.class, admittedBound(GroupAttraction.class));
        assertEquals(IndividualAdmission.class, admittedBound(SpecialAttraction.class));
        TypeVariable<?> parameter = Attraction.class.getTypeParameters()[0];
        assertEquals(Admission.class, parameter.getBounds()[0]);
    }

    private static Object admittedBound(Class<?> attractionClass) {
        return ((ParameterizedType) attractionClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
