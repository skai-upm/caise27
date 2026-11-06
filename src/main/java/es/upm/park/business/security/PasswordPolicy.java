package es.upm.park.business.security;

import es.upm.park.business.exception.InvalidInputException;

/** Validation rules a password must satisfy before it is protected and stored. */
@FunctionalInterface
public interface PasswordPolicy {

    /** @throws InvalidInputException when the password is not acceptable */
    void validate(String rawPassword);

    /** Minimal policy: a password is required and cannot be blank. */
    static PasswordPolicy notBlank() {
        return rawPassword -> {
            if (rawPassword == null || rawPassword.isBlank()) {
                throw new InvalidInputException("The password is required");
            }
        };
    }
}
