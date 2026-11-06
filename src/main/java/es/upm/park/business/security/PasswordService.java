package es.upm.park.business.security;

/**
 * Protects passwords before storage and verifies entered passwords against the
 * stored protected value. The algorithm and the validation rules are injected.
 */
public class PasswordService {

    private final PasswordHasher hasher;
    private final PasswordPolicy policy;

    public PasswordService(PasswordHasher hasher, PasswordPolicy policy) {
        this.hasher = hasher;
        this.policy = policy;
    }

    /** Validates a new or changed password and returns the value to store instead of it. */
    public String protect(String rawPassword) {
        policy.validate(rawPassword);
        return hasher.hash(rawPassword);
    }

    /** Whether an entered password matches the stored protected value. */
    public boolean verify(String rawPassword, String storedHash) {
        return hasher.matches(rawPassword, storedHash);
    }
}
