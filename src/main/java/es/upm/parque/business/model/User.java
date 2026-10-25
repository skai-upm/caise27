package es.upm.parque.business.model;

/**
 * A registered park user. Holds state only; it knows nothing about persistence.
 */
public class User {

    private String name;
    private String surname;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    public User(String name, String surname, String dni, String email,
                String passwordHash, String phone, String card) {
        this.name = name;
        this.surname = surname;
        this.dni = dni;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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

    /** BCrypt hash of the password; the plaintext is never stored. */
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
}
