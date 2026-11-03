package com.parque;

/**
 * A park user. Holds the user's state only; it knows nothing about how users are stored.
 */
public class User {
    private String name;
    private String surname;
    private final String dni;
    private String email;
    private String password;
    private String phone;
    private String card;

    public User(String name, String surname, String dni, String email, String password, String phone, String card) {
        this.name = name;
        this.surname = surname;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.card = card;
    }

    /** Copy constructor, used to apply changes tentatively. */
    public User(User other) {
        this(other.name, other.surname, other.dni, other.email, other.password, other.phone, other.card);
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

    /** Sets the value of an editable field. */
    public void set(UserField field, String value) {
        switch (field) {
            case NAME -> setName(value);
            case SURNAME -> setSurname(value);
            case EMAIL -> setEmail(value);
            case PASSWORD -> setPassword(value);
            case PHONE -> setPhone(value);
            case CARD -> setCard(value);
        }
    }
}
