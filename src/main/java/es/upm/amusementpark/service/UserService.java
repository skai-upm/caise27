package es.upm.amusementpark.service;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.UserValidator;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;

/**
 * User management operations.
 */
public class UserService {

    private final UserRepository repository;
    private final GroupRepository groups;
    private final TicketRepository tickets;

    public UserService(UserRepository repository) {
        this(repository, new GroupRepository());
    }

    public UserService(UserRepository repository, GroupRepository groups) {
        this(repository, groups, new TicketRepository());
    }

    public UserService(UserRepository repository, GroupRepository groups, TicketRepository tickets) {
        this.repository = repository;
        this.groups = groups;
        this.tickets = tickets;
    }

    public TicketRepository tickets() {
        return tickets;
    }

    public UserRepository users() {
        return repository;
    }

    public GroupRepository groups() {
        return groups;
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
        User user = authenticate(email, password);
        var today = Park.getInstance().getCurrentDate();
        if (tickets.individualTicketsOf(user).stream().anyMatch(t -> t.isActiveOn(today))) {
            throw new UserOperationException("the user has active tickets (dated " + today + " or later)");
        }
        for (var group : groups.findAll()) {
            if (group.remove(user)) {
                groups.update(group);
            }
        }
        repository.remove(user);
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
