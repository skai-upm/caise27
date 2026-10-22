package es.upm.etsisi.park.service;

import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.model.UserField;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;

/**
 * Business rules for creating, deleting and updating users.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) {
        require(UserValidator.isNotBlank(firstName), "First name must not be empty");
        require(UserValidator.isNotBlank(surnames), "Surnames must not be empty");
        require(UserValidator.isValidDni(dni), "Invalid DNI");
        require(UserValidator.isValidEmail(email), "Invalid email address");
        require(UserValidator.isValidPassword(password),
                "Invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
        require(UserValidator.isNotBlank(phone), "Phone number must not be empty");
        require(UserValidator.isValidCard(card), "Invalid card number (must be 16 digits)");

        String normalizedDni = dni.toUpperCase();
        require(repository.findByDni(normalizedDni).isEmpty(), "A user with that DNI already exists");
        require(repository.findByEmail(email).isEmpty(), "A user with that email already exists");

        User user = new User(firstName, surnames, normalizedDni, email, password, phone, card);
        repository.add(user);
        return user;
    }

    public void delete(String email, String password) {
        repository.remove(authenticate(email, password));
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromText(fieldName);
        require(field != null, "Field cannot be modified: " + fieldName
                + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");

        switch (field) {
            case NAME -> {
                require(UserValidator.isNotBlank(newValue), "First name must not be empty");
                user.setFirstName(newValue);
            }
            case SURNAME -> {
                require(UserValidator.isNotBlank(newValue), "Surnames must not be empty");
                user.setSurnames(newValue);
            }
            case EMAIL -> {
                require(UserValidator.isValidEmail(newValue), "Invalid email address");
                repository.findByEmail(newValue).ifPresent(other -> require(other == user,
                        "A user with that email already exists"));
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                require(UserValidator.isValidPassword(newValue),
                        "Invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
                user.setPassword(newValue);
            }
            case PHONE -> {
                require(UserValidator.isNotBlank(newValue), "Phone number must not be empty");
                user.setPhone(newValue);
            }
            case CARD -> {
                require(UserValidator.isValidCard(newValue), "Invalid card number (must be 16 digits)");
                user.setCard(newValue);
            }
        }
        repository.save();
    }

    private User authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("User not found: " + email));
        require(user.passwordMatches(password), "Incorrect password");
        return user;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new UserOperationException(message);
        }
    }
}
