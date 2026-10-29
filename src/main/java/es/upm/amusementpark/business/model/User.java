package es.upm.amusementpark.business.model;

import java.util.Objects;

/**
 * A user registered in the park, identified by its email address.
 */
public class User {

    private final String email;
    private final String dni;
    private String passwordHash;

    public User(String email, String dni) {
        this.email = Objects.requireNonNull(email);
        this.dni = Objects.requireNonNull(dni);
    }

    public String getEmail() {
        return email;
    }

    public String getDni() {
        return dni;
    }

    /** @return the BCrypt-protected password, or {@code null} if none is set */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Stores an already protected password value; plain passwords are never stored. */
    public void setPasswordHash(String protectedValue) {
        this.passwordHash = protectedValue;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return email;
    }
}
