package es.upm.amusementpark;

import java.util.List;

/**
 * Business rules for user operations. Every rejection is reported with an IllegalArgumentException
 * whose message explains the reason; nothing is changed in that case.
 */
public class UserService {

    private final Park park = Park.getInstance();

    public User create(String firstName, String surnames, String dni, String email, String password, String phone,
                       String card) {
        if (!UserValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("invalid DNI " + dni);
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("invalid email " + email);
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new IllegalArgumentException(
                    "invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
        }
        if (!UserValidator.isValidCard(card)) {
            throw new IllegalArgumentException("invalid card number (must be exactly 16 digits)");
        }
        UserRepository repository = park.getUsers();
        if (repository.findByDni(dni) != null) {
            throw new IllegalArgumentException("a user with DNI " + dni + " already exists");
        }
        if (repository.findByEmail(email) != null) {
            throw new IllegalArgumentException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        repository.add(user);
        return user;
    }

    /**
     * Deletes a user who holds no active ticket and belongs to no locked group. The user is removed from their
     * groups; historical tickets are kept and still refer to the former user.
     */
    public void delete(String email, String password) {
        User user = authenticate(email, password);
        for (IndividualTicket ticket : park.getTickets().ticketsOf(user)) {
            if (ticket.isActiveOn(park.getDate())) {
                throw new IllegalArgumentException("user " + email + " has active ticket " + ticket.getId());
            }
        }
        List<Group> groups = park.getGroups().groupsOf(user);
        for (Group group : groups) {
            if (park.isLocked(group)) {
                throw new IllegalArgumentException("user " + email + " belongs to group " + group.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        for (Group group : groups) {
            group.removeMember(user);
        }
        park.getGroups().save();
        park.getUsers().remove(user);
        park.getTickets().save();
    }

    public void update(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new IllegalArgumentException("invalid email " + newValue);
                }
                User other = park.getUsers().findByEmail(newValue);
                if (other != null && other != user) {
                    throw new IllegalArgumentException("a user with email " + newValue + " already exists");
                }
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new IllegalArgumentException(
                            "invalid password (needs an uppercase letter, a lowercase letter, a digit and a symbol)");
                }
                user.setPassword(newValue);
            }
            case PHONE -> user.setPhone(newValue);
            case CARD -> {
                if (!UserValidator.isValidCard(newValue)) {
                    throw new IllegalArgumentException("invalid card number (must be exactly 16 digits)");
                }
                user.setCard(newValue);
            }
        }
        park.getUsers().update(user);
    }

    private User authenticate(String email, String password) {
        User user = park.getUsers().findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("incorrect password");
        }
        return user;
    }
}
