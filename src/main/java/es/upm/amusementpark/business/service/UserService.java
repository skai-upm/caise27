package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.RuleViolationException;


import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.UserValidator;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

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
            throw new InvalidDataException("invalid DNI");
        }
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email");
        }
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException("invalid password");
        }
        if (!UserValidator.isValidCardNumber(card)) {
            throw new InvalidDataException("invalid card number");
        }
        if (repository.findByDni(dni).isPresent()) {
            throw new RuleViolationException("a user with this DNI already exists");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new RuleViolationException("a user with this email already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        repository.add(user);
        return user;
    }

    public void deleteUser(String email, String password) {
        User user = authenticate(email, password);
        var today = Park.getInstance().getCurrentDate();
        if (tickets.individualTicketsOf(user).stream().anyMatch(t -> t.isActiveOn(today))) {
            throw new RuleViolationException("the user has active tickets (dated " + today + " or later)");
        }
        for (var group : groups.findAll()) {
            if (group.contains(user) && tickets.isLocked(group, today)) {
                throw new RuleViolationException("the user belongs to group '" + group.getIdentifier()
                        + "', which has an active group ticket");
            }
        }
        for (var group : groups.findAll()) {
            if (group.remove(user)) {
                groups.update(group);
            }
        }
        repository.remove(user);
        tickets.update();
    }

    public void updateUser(String email, String password, UserField field, String newValue) {
        User user = authenticate(email, password);
        switch (field) {
            case NAME -> user.setFirstName(newValue);
            case SURNAME -> user.setSurnames(newValue);
            case PHONE -> user.setTelephone(newValue);
            case EMAIL -> {
                if (!UserValidator.isValidEmail(newValue)) {
                    throw new InvalidDataException("invalid email");
                }
                repository.findByEmail(newValue).filter(other -> other != user).ifPresent(other -> {
                    throw new RuleViolationException("a user with this email already exists");
                });
                user.setEmail(newValue);
            }
            case PASSWORD -> {
                if (!UserValidator.isValidPassword(newValue)) {
                    throw new InvalidDataException("invalid password");
                }
                user.setPassword(newValue);
            }
            case CARD -> {
                if (!UserValidator.isValidCardNumber(newValue)) {
                    throw new InvalidDataException("invalid card number");
                }
                user.setCardNumber(newValue);
            }
            default -> throw new RuleViolationException("field cannot be modified");
        }
        repository.update(user);
    }

    private User authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("user not found"));
        if (!user.getPassword().equals(password)) {
            throw new RuleViolationException("incorrect password");
        }
        return user;
    }
}
