package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class UserFieldTest {

    @Test
    public void knownFieldsAreFound() {
        for (String name : new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"}) {
            assertEquals(name, UserField.fromName(name).name());
        }
    }

    @Test
    public void dniAndUnknownFieldsAreNotFound() {
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("FOO"));
        assertNull(UserField.fromName(""));
    }
}
