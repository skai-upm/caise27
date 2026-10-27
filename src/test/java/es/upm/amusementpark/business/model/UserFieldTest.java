package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void recognisesModifiableFields() {
        assertEquals(6, UserField.values().length);
        for (String name : new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"}) {
            assertEquals(name, UserField.fromText(name).name());
        }
    }

    @Test
    void dniAndUnknownFieldsAreNotModifiable() {
        assertNull(UserField.fromText("DNI"));
        assertNull(UserField.fromText("AGE"));
        assertNull(UserField.fromText("name"));
    }
}
