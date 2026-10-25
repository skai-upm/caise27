package es.upm.parque.business.model;

/**
 * A registered park user. The DNI cannot change once the user is created.
 * The password is kept only as a hash.
 */
public class User {

    private String name;
    private String surnames;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    public User(String name, String surnames, String dni, String email,
                String passwordHash, String phone, String card) {
        this.name = name;
        this.surnames = surnames;
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
}
