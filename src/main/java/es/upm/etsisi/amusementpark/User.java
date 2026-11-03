package es.upm.etsisi.amusementpark;

/**
 * A registered user of the park. Holds state only; it knows nothing about persistence.
 * The DNI is fixed at creation time and cannot be changed afterwards.
 */
public class User {

    private String name;
    private String surname;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String card;

    public User(String name, String surname, String dni, String email,
                String password, String phone, String card) {
        this.name = name;
        this.surname = surname;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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

    /** Returns the current value of an editable field. */
    public String get(UserField field) {
        return switch (field) {
            case NAME -> name;
            case SURNAME -> surname;
            case EMAIL -> email;
            case PASSWORD -> password;
            case PHONE -> phone;
            case CARD -> card;
        };
    }

    /** Sets the value of an editable field. The DNI is not an editable field. */
    public void set(UserField field, String value) {
        switch (field) {
            case NAME -> name = value;
            case SURNAME -> surname = value;
            case EMAIL -> email = value;
            case PASSWORD -> password = value;
            case PHONE -> phone = value;
            case CARD -> card = value;
        }
    }
}
