package es.upm.parque;

import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.business.security.PasswordHasher;

/** Shared test helpers. */
public final class TestSupport {

    /** BCrypt with the minimum work factor, so tests stay fast. */
    public static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    private TestSupport() {
    }
}
