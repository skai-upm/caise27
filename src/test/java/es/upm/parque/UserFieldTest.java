package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void parsesIgnoringCase() {
        assertEquals(UserField.NAME, UserField.parse("name"));
        assertEquals(UserField.CARD, UserField.parse(" CARD "));
        assertEquals(UserField.PHONE, UserField.parse("Phone"));
    }

    @Test
    void dniAndUnknownFieldsAreNotEditable() {
        assertNull(UserField.parse("DNI"));
        assertNull(UserField.parse("AGE"));
        assertNull(UserField.parse(""));
    }
}
