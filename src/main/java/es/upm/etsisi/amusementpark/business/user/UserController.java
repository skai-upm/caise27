package es.upm.etsisi.amusementpark.business.user;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.group.GroupController;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.ticket.TicketController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Singleton controller for user registration, modification and deletion. Passwords are
 * validated in plaintext and stored only through the injected {@link PasswordHasher}.
 */
public final class UserController {

    private Park park;
    private PasswordHasher hasher;
    private TicketController tickets;
    private GroupController groups;

    private UserController() {
    }

    private static final class Holder {
        private static final UserController INSTANCE = new UserController();
    }

    public static UserController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park parkState, PasswordHasher passwordHasher, TicketController ticketController,
                          GroupController groupController) {
        park = parkState;
        hasher = passwordHasher;
        tickets = ticketController;
        groups = groupController;
    }

    public User create(String firstName, String surnames, String dni, String email, String password,
                       String phone, String card) throws BusinessException {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("invalid DNI");
        }
        checkEmail(email, null);
        checkPassword(password);
        checkCard(card);
        if (park.users().stream().anyMatch(u -> u.getDni().equals(dni))) {
            throw new OperationNotAllowedException("DNI already registered");
        }
        User user = new User(firstName, surnames, dni, email, hasher.hash(password), phone, card);
        park.users().add(user);
        park.commit(() -> park.users().remove(user));
        return user;
    }

    /** Verifies the credentials, returning the authenticated user. */
    public User authenticate(String email, String password) throws BusinessException {
        User user = park.requireUser(email);
        if (!hasher.matches(password, user.getPasswordHash())) {
            throw new OperationNotAllowedException("incorrect password");
        }
        return user;
    }

    public void update(String email, String password, UserField field, String value) throws BusinessException {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> apply(user, User::setFirstName, user.getFirstName(), value);
            case SURNAME -> apply(user, User::setSurnames, user.getSurnames(), value);
            case EMAIL -> {
                checkEmail(value, user);
                apply(user, User::setEmail, user.getEmail(), value);
            }
            case PASSWORD -> {
                checkPassword(value);
                apply(user, User::setPasswordHash, user.getPasswordHash(), hasher.hash(value));
            }
            case PHONE -> apply(user, User::setPhone, user.getPhone(), value);
            case CARD -> {
                checkCard(value);
                apply(user, User::setCard, user.getCard(), value);
            }
        }
    }

    /**
     * Deletes a user unless they hold an active ticket or belong to a locked group; on success
     * the user also leaves their groups and attractions. Historical tickets are kept.
     */
    public void delete(String email, String password) throws BusinessException {
        User user = authenticate(email, password);
        if (tickets.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("user has active tickets");
        }
        List<Group> memberships = groups.groupsOf(user);
        if (memberships.stream().anyMatch(groups::isLocked)) {
            throw new OperationNotAllowedException("user belongs to a group with an active group ticket");
        }
        Map<Group, Integer> positions = new LinkedHashMap<>();
        memberships.forEach(g -> positions.put(g, g.removeMember(user)));
        List<Attraction<?>> inside = park.attractions().stream().filter(a -> a.release(user)).toList();
        int index = park.users().indexOf(user);
        park.users().remove(index);
        park.commit(() -> {
            park.users().add(index, user);
            positions.forEach((g, position) -> g.insertMember(position, user));
            inside.forEach(a -> a.admit(List.of(user)));
        });
    }

    private void apply(User user, BiConsumer<User, String> setter, String previous, String value)
            throws BusinessException {
        setter.accept(user, value);
        park.commit(() -> setter.accept(user, previous));
    }

    private void checkEmail(String email, User owner) throws BusinessException {
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email");
        }
        if (park.findUser(email).filter(u -> u != owner).isPresent()) {
            throw new OperationNotAllowedException("email already registered");
        }
    }

    private static void checkPassword(String password) throws InvalidDataException {
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException("invalid password");
        }
    }

    private static void checkCard(String card) throws InvalidDataException {
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("invalid card number");
        }
    }
}
