package es.upm.etsisi.park.business;

/**
 * A registered park user. The DNI is fixed at creation time and the password is
 * kept only as a BCrypt hash.
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

    /** Copy constructor, used to roll back a modification. */
    public User(User other) {
        this(other.firstName, other.surnames, other.dni, other.email,
                other.passwordHash, other.phone, other.card);
    }

    public String getFirstName() {
        return firstName;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurnames() {
        return surnames;
    }

    void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCard() {
        return card;
    }

    void setCard(String card) {
        this.card = card;
    }

    public boolean checkPassword(String candidate) {
        return PasswordHasher.matches(candidate, passwordHash);
    }
}
