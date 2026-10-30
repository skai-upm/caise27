package es.upm.amusementpark;

/**
 * Business rules for user operations. Every rejection is reported with an IllegalArgumentException
 * whose message explains the reason; nothing is changed in that case.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

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

    public void delete(String email, String password) {
        User user = authenticate(email, password);
        repository.remove(user);
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
                User other = repository.findByEmail(newValue);
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
        repository.update(user);
    }

    private User authenticate(String email, String password) {
        User user = repository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("incorrect password");
        }
        return user;
    }
}
