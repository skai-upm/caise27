package es.upm.amusementpark;

import java.util.List;

/**
 * User management operations. Applies the validation and uniqueness rules
 * before changing the stored users.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(String firstName, String surnames, String dni, String email,
            String password, String phone, String card) throws UserOperationException {
        List<String> errors = UserValidator.validate(dni, email, password, card);
        if (!errors.isEmpty()) {
            throw new UserOperationException(String.join(" ", errors));
        }
        if (repository.existsByDni(dni)) {
            throw new UserOperationException("A user with DNI " + dni + " already exists.");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new UserOperationException("A user with email " + email + " already exists.");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        repository.save(user);
        return user;
    }

    public void deleteUser(String email, String password) throws UserOperationException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("No user with email " + email + " exists."));
        if (!user.getPassword().equals(password)) {
            throw new UserOperationException("Incorrect password.");
        }
        repository.delete(email);
    }
}
