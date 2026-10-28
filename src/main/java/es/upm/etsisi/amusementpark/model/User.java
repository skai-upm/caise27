package es.upm.etsisi.amusementpark.model;

/**
 * A user registered in the park.
 */
public class User {

    private final String email;

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
