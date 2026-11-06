package es.upm.park.business.security;

/** One-way protection of passwords for storage. */
public interface PasswordHasher {

    /** A new protected value for the password (salted). */
    String hash(String rawPassword);

    /** Whether the password corresponds to a stored protected value. */
    boolean matches(String rawPassword, String storedHash);
}
