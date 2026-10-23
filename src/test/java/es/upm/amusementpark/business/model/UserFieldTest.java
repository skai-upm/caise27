package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void parsesAllModifiableFieldsIgnoringCase() {
        assertEquals(UserField.NAME, UserField.fromString("name"));
        assertEquals(UserField.SURNAME, UserField.fromString("Surname"));
        assertEquals(UserField.EMAIL, UserField.fromString("EMAIL"));
        assertEquals(UserField.PASSWORD, UserField.fromString(" password "));
        assertEquals(UserField.PHONE, UserField.fromString("PHONE"));
        assertEquals(UserField.CARD, UserField.fromString("card"));
        assertEquals(6, UserField.values().length);
    }

    @Test
    void rejectsDniAndUnknownNames() {
        assertNull(UserField.fromString("DNI"));
        assertNull(UserField.fromString("AGE"));
        assertNull(UserField.fromString(""));
        assertNull(UserField.fromString(null));
    }

    @Test
    void listsAllNames() {
        assertEquals("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD", UserField.allNames());
    }
}
