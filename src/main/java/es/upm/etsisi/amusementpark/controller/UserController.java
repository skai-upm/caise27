package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.User;
import es.upm.etsisi.amusementpark.model.UserField;
import es.upm.etsisi.amusementpark.repository.UserRepository;
import es.upm.etsisi.amusementpark.validation.UserValidator;

import java.util.List;

/**
 * User management operations. Rule violations are reported as IllegalArgumentException.
 */
public class UserController {

    private final ParkController park;

    public UserController(ParkController park) {
        this.park = park;
    }

    public User create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String cardNumber) {
        UserRepository repository = park.users();
        requireValid(UserValidator.isValidDni(dni), "Invalid DNI");
        requireValid(UserValidator.isValidEmail(email), "Invalid email");
        requireValid(UserValidator.isValidPassword(password), "Invalid password");
        requireValid(UserValidator.isValidCardNumber(cardNumber), "Invalid card number");
        requireValid(repository.findByDni(dni).isEmpty(), "DNI already registered");
        requireValid(repository.findByEmail(email).isEmpty(), "Email already registered");

        User user = new User(firstName, surnames, dni, email, password, phone, cardNumber);
        repository.add(user);
        return user;
    }

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        requireValid(!park.tickets().hasActiveTicketFor(user, park.today()),
                "User has active tickets");
        List<Group> groups = park.groups().findByMember(user);
        for (Group group : groups) {
            requireValid(!park.tickets().hasActiveGroupTicket(group, park.today()),
                    "User belongs to locked group " + group.getId());
        }
        groups.forEach(g -> g.remove(user));
        park.users().remove(user);
        park.groups().save();
        park.tickets().save();
    }

    public void update(String email, String password, String fieldName, String newValue) {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        requireValid(field != null, "Field cannot be modified: " + fieldName);

        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                requireValid(UserValidator.isValidEmail(newValue), "Invalid email");
                park.users().findByEmail(newValue)
                        .filter(other -> other != user)
                        .ifPresent(other -> {
                            throw new IllegalArgumentException("Email already registered");
                        });
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                requireValid(UserValidator.isValidPassword(newValue), "Invalid password");
                user.setPassword(newValue);
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                requireValid(UserValidator.isValidCardNumber(newValue), "Invalid card number");
                user.setCardNumber(newValue);
            }
        }
        park.users().update(user);
        park.tickets().save();
    }

    private User authenticate(String email, String password) {
        User user = park.users().findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        requireValid(user.checkPassword(password), "Incorrect password");
        return user;
    }

    static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
