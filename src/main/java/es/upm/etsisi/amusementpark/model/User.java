package es.upm.etsisi.amusementpark.model;

/**
 * A user registered in the park. Users do not hold references to their tickets.
 */
public class User {

    private final String dni;
    private final String email;

    public User(String dni, String email) {
        this.dni = dni;
        this.email = email;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }
}
