package es.upm.amusementpark.business.model;

import java.util.Objects;

/**
 * A user registered in the park. The DNI identifies the user and cannot change;
 * the password is only kept in its BCrypt-protected form.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    public User(String firstName, String surnames, String dni, String email, String passwordHash,
            String phone, String card) {
        this.firstName = Objects.requireNonNull(firstName);
        this.surnames = Objects.requireNonNull(surnames);
        this.dni = Objects.requireNonNull(dni);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.phone = Objects.requireNonNull(phone);
        this.card = Objects.requireNonNull(card);
    }

    /** A user known only by email and DNI (for example, the holder of a historical ticket). */
    public User(String email, String dni) {
        this("", "", dni, email, null, "", "");
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(firstName);
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = Objects.requireNonNull(surnames);
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email);
    }

    /** @return the BCrypt-protected password, or {@code null} if none is set */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** Stores an already protected password value; plain passwords are never stored. */
    public void setPasswordHash(String protectedValue) {
        this.passwordHash = protectedValue;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = Objects.requireNonNull(phone);
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = Objects.requireNonNull(card);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && dni.equals(user.dni);
    }

    @Override
    public int hashCode() {
        return dni.hashCode();
    }

    @Override
    public String toString() {
        return email;
    }
}
