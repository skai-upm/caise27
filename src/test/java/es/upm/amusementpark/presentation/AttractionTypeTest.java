package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GeneralAttraction;
import es.upm.amusementpark.business.model.GroupAccess;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.SpecialAttraction;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionTypeTest extends ParkTestSupport {

    @BeforeEach
    void tickets() {
        run(ANA);
        run(PEDRO);
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;pedro@correo.com");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-01");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-01");
        run("ATTRACTION CREATE;GEN;General;10");
        run("ATTRACTION CREATE;IND;Individual;10;INDIVIDUAL");
        run("ATTRACTION CREATE;GRP;Grupo;10;GROUP");
        run("ATTRACTION CREATE;SPE;Especial;10;SPECIAL");
        run("ATTRACTION CREATE;GEX;Explicit;10;GENERAL");
    }

    private Attraction<?> attraction(String id) {
        return park.getAttractions().find(id);
    }

    @Test
    void e2SyntaxCreatesGeneralAndTypeCanBeGiven() {
        assertEquals(AttractionType.GENERAL, attraction("GEN").getType());
        assertEquals(AttractionType.GENERAL, attraction("GEX").getType());
        assertEquals(AttractionType.INDIVIDUAL, attraction("IND").getType());
        assertEquals(AttractionType.GROUP, attraction("GRP").getType());
        assertEquals(AttractionType.SPECIAL, attraction("SPE").getType());
        assertTrue(run("ATTRACTION STATUS;SPE").contains("(SPECIAL)"));
    }

    @Test
    void rejectsUnknownType() {
        assertTrue(error(run("ATTRACTION CREATE;X;Vip;5;VIP")));
        assertTrue(error(run("ATTRACTION CREATE;X;Vip;5;special")));
        assertNull(attraction("X"));
    }

    @ParameterizedTest
    @CsvSource({"GEN,T001,true", "GEN,TG01,true", "IND,T001,true", "IND,TG01,false",
            "GRP,T001,false", "GRP,TG01,true", "SPE,TG01,false", "GEN,TG01-1,true", "GRP,TG01-1,false"})
    void typesAdmitOnlyTheirTicketKinds(String attractionId, String ticketId, boolean admitted) {
        String result = run("ATTRACTION ENTER;" + attractionId + ";" + ticketId);
        assertEquals(admitted, ok(result), result);
        assertEquals(admitted, !attraction(attractionId).getVisitors().isEmpty());
    }

    @Test
    void typePersistsAndLegacyLinesLoadAsGeneral() throws IOException {
        restart();
        assertEquals(AttractionType.SPECIAL, attraction("SPE").getType());
        assertEquals(AttractionType.GROUP, attraction("GRP").getType());

        Files.writeString(dir.resolve("attractions.txt"), "A01;Tornado;20\n");
        restart();
        assertEquals(AttractionType.GENERAL, attraction("A01").getType());
        assertEquals(20, attraction("A01").getCapacity());
    }

    @Test
    void genericBoundsMatchTheAdmittedCapability() {
        assertEquals(AccessTicket.class, boundOf(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(IndividualAttraction.class));
        assertEquals(GroupAccess.class, boundOf(GroupAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(SpecialAttraction.class));
        assertFalse(new GroupAttraction("G", "g", 1).admits(park.getTickets().find("T001")));
    }

    private static Object boundOf(Class<?> type) {
        return ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
