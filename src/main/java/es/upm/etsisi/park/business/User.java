package es.upm.etsisi.park.business;

/**
 * A registered park user. The DNI is fixed at creation; the password attribute holds the
 * BCrypt hash of the password. Other attributes change only through {@link UserController}.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String card;

    public User(String firstName, String surnames, String dni, String email,
                String password, String phone, String card) {
        this.firstName = firstName;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.card = card;
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

    /** The stored (hashed) password. */
    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getCard() {
        return card;
    }


    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setPassword(String password) {
        this.password = password;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    void setCard(String card) {
        this.card = card;
    }
}
