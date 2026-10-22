package es.upm.etsisi.park.business.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserFieldTest {

    @Test
    void resolvesFieldsCaseInsensitively() {
        assertEquals(UserField.PHONE, UserField.fromText("PHONE"));
        assertEquals(UserField.EMAIL, UserField.fromText("email"));
        assertEquals(UserField.NAME, UserField.fromText(" Name "));
    }

    @Test
    void dniIsNotAnUpdatableField() {
        assertNull(UserField.fromText("DNI"));
        assertNull(UserField.fromText("AGE"));
        assertNull(UserField.fromText(null));
    }
}
