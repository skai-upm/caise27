package es.upm.park;

/**
 * A registered park user. The DNI is fixed at creation time; every other field is editable.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String cardNumber;

    public User(String firstName, String surnames, String dni, String email,
                String password, String phone, String cardNumber) {
        this.firstName = firstName;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.cardNumber = cardNumber;
    }

    /** Creates an independent copy of another user. */
    public User(User other) {
        this(other.firstName, other.surnames, other.dni, other.email,
                other.password, other.phone, other.cardNumber);
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

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getCardNumber() {
        return cardNumber;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
