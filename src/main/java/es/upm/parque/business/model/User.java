package es.upm.parque.business.model;

/**
 * A user of the amusement park system.
 *
 * <p>All attributes are encapsulated. The DNI identifies the user and cannot be
 * changed after creation. This class only holds data: it performs no persistence.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String password;
    private String telephone;
    private String cardNumber;

    public User(String firstName, String surnames, String dni, String email,
                String password, String telephone, String cardNumber) {
        this.firstName = firstName;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.cardNumber = cardNumber;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
