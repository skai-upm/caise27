package es.upm.etsisi.park.service;

import es.upm.etsisi.park.core.SystemState;
import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.model.UserField;
import es.upm.etsisi.park.repository.AttractionRepository;
import es.upm.etsisi.park.repository.GroupRepository;
import es.upm.etsisi.park.repository.TicketRepository;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.validation.UserValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Business rules for creating, deleting and updating users.
 */
public class UserService {

    private final UserRepository repository;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;
    private final SystemState state;

    public UserService(UserRepository repository, GroupRepository groups, TicketRepository tickets,
                       AttractionRepository attractions, SystemState state) {
        this.repository = repository;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.state = state;
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

    /**
     * Deletes a user who has no active tickets and belongs to no locked group. The user is
     * removed from their groups; historical tickets keep referring to them.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        LocalDate today = state.getCurrentDate();
        require(!tickets.hasActiveTicket(user, today), "User has active tickets and cannot be deleted");
        List<Group> memberships = groups.findByMember(user);
        for (Group group : memberships) {
            require(!tickets.hasActiveGroupTicket(group, today),
                    "User belongs to group " + group.getId() + ", which has active group tickets");
        }
        for (Group group : memberships) {
            group.remove(user);
        }
        attractions.findContaining(user).ifPresent(attraction -> attraction.exit(user));
        groups.save();
        tickets.save();
        repository.remove(user);
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
        tickets.save();
    }

    private User authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("User not found: " + email));
        require(user.passwordMatches(password), "Incorrect password");
        return user;
    }

    private static void require(boolean condition, String message) {
        Checks.require(condition, message);
    }
}
