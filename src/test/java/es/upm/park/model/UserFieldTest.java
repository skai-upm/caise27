package es.upm.park.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserFieldTest {

    @Test
    void parsesIgnoringCaseAndWhitespace() {
        assertEquals(UserField.PHONE, UserField.parse(" phone "));
        assertEquals(UserField.NAME, UserField.parse("NAME"));
        assertEquals(UserField.CARD, UserField.parse("Card"));
    }

    @Test
    void dniIsNotModifiable() {
        assertNull(UserField.parse("DNI"));
    }

    @Test
    void unknownOrNullIsRejected() {
        assertNull(UserField.parse("AGE"));
        assertNull(UserField.parse(null));
    }

    @Test
    void listsAllFieldNames() {
        assertEquals("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD", UserField.listNames());
    }
}
