package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserFieldTest {

    @Test
    void allModifiableFieldsParse() {
        for (String name : new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"}) {
            assertEquals(name, UserField.fromName(name).name());
        }
        assertEquals(6, UserField.values().length);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"DNI", "AGE", "name", ""})
    void otherNamesAreNotModifiable(String name) {
        assertNull(UserField.fromName(name));
    }
}
