package es.upm.park.business.security;

import es.upm.park.business.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService service = new PasswordService(new BCryptPasswordHasher(), PasswordPolicy.notBlank());

    @Test
    void protectedValueIsBCryptWithoutThePassword() {
        String stored = service.protect("Clave1!");
        assertTrue(stored.startsWith("$2a$"));
        assertFalse(stored.contains("Clave1!"));
    }

    @Test
    void saltingGivesDifferentValuesThatBothVerify() {
        String first = service.protect("Clave1!");
        String second = service.protect("Clave1!");
        assertNotEquals(first, second);
        assertTrue(service.verify("Clave1!", first));
        assertTrue(service.verify("Clave1!", second));
    }

    @Test
    void wrongPasswordOrMalformedHashFails() {
        String stored = service.protect("Clave1!");
        assertFalse(service.verify("Clave2!", stored));
        assertFalse(service.verify("Clave1!", "Clave1!"));
        assertFalse(service.verify("Clave1!", null));
    }

    @Test
    void blankPasswordsAreRejected() {
        assertThrows(InvalidInputException.class, () -> service.protect(" "));
        assertThrows(InvalidInputException.class, () -> service.protect(null));
    }

    @Test
    void injectedPolicyIsApplied() {
        PasswordService strict = new PasswordService(new BCryptPasswordHasher(), raw -> {
            if (raw.length() < 6) {
                throw new InvalidInputException("Too short");
            }
        });
        assertThrows(InvalidInputException.class, () -> strict.protect("abc"));
        assertTrue(strict.verify("abcdef", strict.protect("abcdef")));
    }
}
