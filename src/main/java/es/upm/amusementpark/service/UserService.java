package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.UserValidator;
import es.upm.amusementpark.repository.UserRepository;

/**
 * User management operations.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) {
        if (!UserValidator.isValidDni(dni)) {
            throw new UserOperationException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new UserOperationException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new UserOperationException("invalid password");
        }
        if (!UserValidator.isValidCardNumber(card)) {
            throw new UserOperationException("invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new UserOperationException("a user with this DNI already exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new UserOperationException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        repository.add(user);
        return user;
    }

    public void deleteUser(String email, String password) {
        repository.remove(authenticate(email, password));
    }

    public void updateUser(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case PHONE -> user.setTelephone(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new UserOperationException("invalid email");
                }
                repository.findByEmail(newValue).filter(other -> other != user).ifPresent(other -> {
                    throw new UserOperationException("a user with this email already exists");
                });
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new UserOperationException("invalid password");
                }
                user.setPassword(newValue);
            }
            case CARD -> {
                if (!UserValidator.isValidCardNumber(newValue)) {
                    throw new UserOperationException("invalid card number");
                }
                user.setCardNumber(newValue);
            }
            default -> throw new UserOperationException("field cannot be modified");
        }
        repository.update(user);
    }

    private User authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserOperationException("user not found"));
        if (!user.getPassword().equals(password)) {
            throw new UserOperationException("incorrect password");
        }
        return user;
    }
}
