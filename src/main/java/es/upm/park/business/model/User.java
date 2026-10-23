package es.upm.park.business.model;

/**
 * A registered park user. The DNI is fixed at creation time; every other
 * attribute can be changed through its setter. Only a protected (hashed)
 * form of the password is kept.
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

    public String getFullName() {
        return firstName + " " + surnames;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + dni + ", " + email + ")";
    }
}
