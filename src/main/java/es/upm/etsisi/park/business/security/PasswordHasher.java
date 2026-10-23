package es.upm.etsisi.park.business.security;

/** Protects passwords before they are stored and checks candidates against them. */
public interface PasswordHasher {

    /** Protected form of a plain password, suitable for storage. */
    String hash(String plainPassword);

    /** Whether the plain password corresponds to the stored protected value. */
    boolean matches(String plainPassword, String storedHash);
}
