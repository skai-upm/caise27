package es.upm.park;

/**
 * A park user. Holds state only; it knows nothing about how it is stored.
 */
public class User {

    private String name;
    private String surnames;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String card;

    public User(String name, String surnames, String dni, String email,
                String password, String phone, String card) {
        this.name = name;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.card = card;
    }

    /** Copy constructor, used to prepare changes without touching the original. */
    public User(User other) {
        this(other.name, other.surnames, other.dni, other.email,
                other.password, other.phone, other.card);
    }

    /** Copies every editable field (all but the DNI) from another user into this one. */
    void copyEditableFrom(User other) {
        this.name = other.name;
        this.surnames = other.surnames;
        this.email = other.email;
        this.password = other.password;
        this.phone = other.phone;
        this.card = other.card;
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
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
}
