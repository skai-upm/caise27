# Design

## Context

`UserValidator.validate(dni, email, password, card)` (F1-C02), `PasswordHasher` injected in `ParkController` (F3-C05), users indexed by e-mail in `ParkController`, commands extend `ControllerCommand<ParkController>` and state is saved after every command.

## Decisions

- **`ParkController.registerUser(firstName, surnames, dni, email, password, phone, card)`**: `UserValidator.validate` → DNI uniqueness (scan of active users) → e-mail uniqueness (index) → `new User(firstName, surnames, dni, email, passwordHasher.hash(password), phone, card)` → `addUser`. All checks precede any change. Duplicates raise `AlreadyExistsException`.
- **`UserCreateCommand`** with seven required non-empty fields, message `USER CREATED`, registered as `USER CREATE`. HELP already lists the syntax.
