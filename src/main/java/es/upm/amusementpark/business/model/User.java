package es.upm.amusementpark.business.model;

/**
 * A registered park user. The DNI is fixed at creation; the other attributes
 * may be changed through {@link UserField}.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    /**
     * @param passwordHash the BCrypt hash of the password, never the password itself
     */
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

    /** Returns a copy of {@code other}, so changes can be prepared without touching the original. */
    public User(User other) {
        this(other.firstName, other.surnames, other.dni, other.email,
                other.passwordHash, other.phone, other.card);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurnames() {
        return surnames;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getCard() {
        return card;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCard(String card) {
        this.card = card;
    }
}
