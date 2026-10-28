package es.upm.etsisi.amusementpark.business.model;

/**
 * A user registered in the park. Users do not hold references to their tickets.
 */
public class User {

    private final String dni;
    private final String email;
    private String passwordHash;

    public User(String dni, String email) {
        this(dni, email, null);
    }

    /**
     * @param passwordHash protected (BCrypt) value of the password, or {@code null} if none is stored
     */
    public User(String dni, String email, String passwordHash) {
        this.dni = dni;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    /** Protected value of the password; the original password is never kept. */
    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
