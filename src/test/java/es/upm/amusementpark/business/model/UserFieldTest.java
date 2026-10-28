package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void exactlyTheSixModifiableFieldsExist() {
        assertEquals(List.of("NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"),
                Arrays.stream(UserField.values()).map(Enum::name).toList());
    }

    @Test
    void lookupByName() {
        assertEquals(UserField.CARD, UserField.fromName("CARD").orElseThrow());
        assertTrue(UserField.fromName("DNI").isEmpty());
        assertTrue(UserField.fromName("name").isEmpty());
    }
}
