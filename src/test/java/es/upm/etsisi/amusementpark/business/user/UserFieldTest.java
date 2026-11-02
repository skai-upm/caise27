package es.upm.etsisi.amusementpark.business.user;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void definesExactlyTheModifiableFields() {
        assertArrayEquals(new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"},
                java.util.Arrays.stream(UserField.values()).map(Enum::name).toArray(String[]::new));
    }

    @Test
    void resolvesByExactName() {
        assertEquals(UserField.EMAIL, UserField.fromName("EMAIL"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("email"));
    }
}
