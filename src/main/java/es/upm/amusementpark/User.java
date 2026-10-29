package es.upm.amusementpark;

import java.util.Objects;

/**
 * A user registered in the park, identified by its email address.
 */
public class User {

    private final String email;

    public User(String email) {
        this.email = Objects.requireNonNull(email);
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return email;
    }
}
