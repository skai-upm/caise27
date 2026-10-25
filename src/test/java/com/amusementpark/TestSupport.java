package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.security.PasswordHasher;

import java.util.List;

/**
 * Shared helpers for tests.
 */
public final class TestSupport {
    /** A cheap hasher so tests that create many users stay fast. */
    public static final PasswordHasher FAST_HASHER = new PasswordHasher(4);

    private TestSupport() {
    }

    /**
     * Compares stored user lines with expected lines written with plain-text passwords: every
     * field must match except the password, which must be a hash of the expected password.
     */
    public static void assertStoredUsers(List<String> expected, List<String> actual) {
        assertEquals(expected.size(), actual.size(), actual.toString());
        for (int i = 0; i < expected.size(); i++) {
            String[] want = expected.get(i).split(";", -1);
            String[] got = actual.get(i).split(";", -1);
            assertEquals(want.length, got.length, actual.get(i));
            for (int f = 0; f < want.length; f++) {
                if (f == 4) {
                    assertNotEquals(want[f], got[f], "password stored in plain text");
                    assertTrue(FAST_HASHER.matches(want[f], got[f]), "hash does not verify " + want[f]);
                } else {
                    assertEquals(want[f], got[f], actual.get(i));
                }
            }
        }
    }
}
