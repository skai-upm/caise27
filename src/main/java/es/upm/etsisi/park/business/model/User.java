package es.upm.etsisi.park.business.model;

import java.util.Objects;

/**
 * A registered park user. The DNI is fixed at creation time.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    public User(String firstName, String surnames, String dni, String email,
                String passwordHash, String phone, String card) {
        this.firstName = firstName;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.card = card;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return dni.equals(other.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

    @Override
    public String toString() {
        return firstName + " " + surnames + " (" + email + ")";
    }
}
