package es.upm.amusementpark.business;

import java.util.Objects;

/**
 * Registered park user. The DNI never changes after creation; the password attribute holds the stored
 * (protected) password value. Users do not manage their own persistence.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String card;

    public User(String firstName, String surnames, String dni, String email, String password, String phone,
            String card) {
        this.firstName = required(firstName, "firstName");
        this.surnames = required(surnames, "surnames");
        this.dni = required(dni, "dni");
        this.email = required(email, "email");
        this.password = required(password, "password");
        this.phone = required(phone, "phone");
        this.card = required(card, "card");
    }

    private static String required(String value, String name) {
        return Objects.requireNonNull(value, name);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = required(firstName, "firstName");
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = required(surnames, "surnames");
    }

    /** @return the DNI, fixed at creation */
    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = required(email, "email");
    }

    /** @return the stored (protected) password value */
    public String getPassword() {
        return password;
    }

    /** @param password the new stored (protected) password value */
    public void setPassword(String password) {
        this.password = required(password, "password");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = required(phone, "phone");
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = required(card, "card");
    }
}
