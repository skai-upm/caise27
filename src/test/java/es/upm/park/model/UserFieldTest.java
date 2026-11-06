package es.upm.park.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void definesExactlyTheModifiableFields() {
        assertArrayEquals(new UserField[] {UserField.NAME, UserField.SURNAME, UserField.EMAIL,
            UserField.PASSWORD, UserField.PHONE, UserField.CARD}, UserField.values());
    }

    @Test
    void resolvesNamesExactly() {
        assertEquals(UserField.CARD, UserField.fromName("CARD"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("card"));
        assertNull(UserField.fromName(null));
    }
}
