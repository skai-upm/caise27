package es.upm.etsisi.amusementpark.business.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserFieldTest {

    @Test
    void exactlyTheModifiableFields() {
        assertEquals(List.of("NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"),
                Arrays.stream(UserField.values()).map(Enum::name).toList());
    }

    @Test
    void lookup() {
        assertEquals(Optional.of(UserField.EMAIL), UserField.fromName("EMAIL"));
        assertTrue(UserField.fromName("DNI").isEmpty());
        assertTrue(UserField.fromName("email").isEmpty());
    }
}
