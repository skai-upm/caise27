package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;

/** Shared test fixtures. */
public final class TestSupport {

    /** Real BCrypt with the minimum work factor, to keep tests fast. */
    public static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestSupport() {
    }
}
