package es.upm.etsisi.park.user;

/**
 * A registered park user. The DNI is fixed at creation time; every other
 * attribute can be changed through its setter.
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

    public boolean checkPassword(String candidate) {
        return password.equals(candidate);
    }

    /** Returns the current value of a modifiable field. */
    public String get(UserField field) {
        return switch (field) {
            case NAME -> firstName;
            case SURNAME -> surnames;
            case EMAIL -> email;
            case PASSWORD -> password;
            case PHONE -> phone;
            case CARD -> card;
        };
    }

    /** Sets a modifiable field. Values are expected to be validated by the caller. */
    public void set(UserField field, String value) {
        switch (field) {
            case NAME -> firstName = value;
            case SURNAME -> surnames = value;
            case EMAIL -> email = value;
            case PASSWORD -> password = value;
            case PHONE -> phone = value;
            case CARD -> card = value;
        }
    }

    @Override
    public String toString() {
        return firstName + " " + surnames + " (" + dni + ", " + email + ")";
    }
}
