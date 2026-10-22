package es.upm.amusementpark.model;

/**
 * A registered park user. The DNI is fixed at creation; the other attributes
 * may be changed through {@link UserField}.
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

    /** Returns a copy of {@code other}, so changes can be prepared without touching the original. */
    public User(User other) {
        this(other.firstName, other.surnames, other.dni, other.email,
                other.password, other.phone, other.card);
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public boolean checkPassword(String candidate) {
        return password.equals(candidate);
    }
}
