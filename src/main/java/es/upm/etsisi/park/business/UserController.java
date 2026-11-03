package es.upm.etsisi.park.business;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User management. Every operation validates first and only then changes state;
 * if persisting fails the in-memory change is undone. Passwords are stored only
 * as BCrypt hashes.
 */
public final class UserController {

    private static final UserController INSTANCE = new UserController();

    private Park park;

    private UserController() {
    }

    public static UserController getInstance() {
        return INSTANCE;
    }

    public void configure(Park park) {
        this.park = park;
    }

    public void create(String firstName, String surnames, String dni, String email,
                       String password, String phone, String card) throws ParkException {
        validateDni(dni);
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        if (park.findUserByDni(dni) != null) {
            throw new ValidationException("a user with DNI " + dni + " already exists");
        }
        if (park.findUser(email) != null) {
            throw new ValidationException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, PasswordHasher.hash(password), phone, card);
        park.addUser(user);
        park.commit(() -> park.removeUser(user));
    }

    public void delete(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (park.hasActiveTicket(user)) {
            throw new OperationNotAllowedException("user " + email + " has active tickets and cannot be deleted");
        }
        List<Group> groups = park.groupsOf(user);
        for (Group g : groups) {
            if (park.isGroupLocked(g)) {
                throw new OperationNotAllowedException("user " + email + " belongs to group " + g.getId()
                        + ", which is locked by an active group ticket");
            }
        }
        Map<String, User> usersBefore = park.snapshotUsers();
        Map<Group, Integer> positions = new LinkedHashMap<>();
        for (Group g : groups) {
            positions.put(g, g.indexOf(user));
            g.remove(user);
        }
        Map<Attraction<? extends GeneralAccess>, List<User>> occupancy = new LinkedHashMap<>();
        for (Attraction<? extends GeneralAccess> a : park.getAttractions()) {
            if (a.isInside(user)) {
                occupancy.put(a, a.getOccupants());
                a.exit(user);
            }
        }
        park.removeUser(user);
        park.commit(() -> {
            park.replaceUsers(usersBefore);
            positions.forEach((g, index) -> g.add(index, user));
            occupancy.forEach(Attraction::restore);
        });
    }

    public void update(String email, String password, String fieldName, String value)
            throws ParkException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ValidationException("field " + fieldName + " cannot be modified");
        }
        validateNewValue(user, field, value);

        Map<String, User> usersBefore = park.snapshotUsers();
        User original = new User(user);
        applyField(user, field, value);
        if (field == UserField.EMAIL) {
            rekey(email, user);
        }
        park.commit(() -> {
            applyAll(user, original);
            park.replaceUsers(usersBefore);
        });
    }

    private void validateNewValue(User user, UserField field, String value) throws ValidationException {
        switch (field) {
            case EMAIL -> {
                validateEmail(value);
                User other = park.findUser(value);
                if (other != null && other != user) {
                    throw new ValidationException("a user with email " + value + " already exists");
                }
            }
            case PASSWORD -> validatePassword(value);
            case CARD -> validateCard(value);
            default -> {
                // NAME, SURNAME and PHONE only need to be stored.
            }
        }
    }

    private static void applyField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPasswordHash(PasswordHasher.hash(value));
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private static void applyAll(User target, User source) {
        target.setFirstName(source.getFirstName());
        target.setSurnames(source.getSurnames());
        target.setEmail(source.getEmail());
        target.setPasswordHash(source.getPasswordHash());
        target.setPhone(source.getPhone());
        target.setCard(source.getCard());
    }

    /** Re-inserts the user under its new email, keeping the original order. */
    private void rekey(String oldEmail, User user) {
        Map<String, User> reordered = new LinkedHashMap<>();
        for (Map.Entry<String, User> e : park.snapshotUsers().entrySet()) {
            reordered.put(e.getKey().equals(oldEmail) ? user.getEmail() : e.getKey(), e.getValue());
        }
        park.replaceUsers(reordered);
    }

    private User authenticate(String email, String password) throws ParkException {
        User user = park.findUser(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new OperationNotAllowedException("incorrect password");
        }
        return user;
    }

    private static void validateDni(String dni) throws ValidationException {
        if (!UserValidator.isValidDni(dni)) {
            throw new ValidationException("invalid DNI " + dni);
        }
    }

    private static void validateEmail(String email) throws ValidationException {
        if (!UserValidator.isValidEmail(email)) {
            throw new ValidationException("invalid email " + email);
        }
    }

    private static void validatePassword(String password) throws ValidationException {
        if (!UserValidator.isValidPassword(password)) {
            throw new ValidationException(
                    "invalid password: it needs an uppercase letter, a lowercase letter, a digit and a symbol");
        }
    }

    private static void validateCard(String card) throws ValidationException {
        if (!UserValidator.isValidCard(card)) {
            throw new ValidationException("invalid card number: it must have exactly 16 digits");
        }
    }
}
