package es.upm.park.business.security;

/**
 * Protects passwords before they are stored and checks candidates against
 * stored values.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    /**
     * @return {@code false} for a wrong password or a stored value that is
     *         not a hash this hasher understands
     */
    boolean matches(String plainPassword, String storedHash);
}
