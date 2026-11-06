# Design

## Context

`ParkController` asks a `UserDirectory` port for user existence and DNI. The only implementations so far are the default "no users" lambda and test stubs. `PasswordService` produces BCrypt values. Layers: business holds model and controllers, persistence holds file repositories, and `App`/`AppConfiguration` are the composition root.

## Goals / Non-Goals

**Goals:** an encapsulated `User` with an immutable DNI, a store separate from both the user class and `App`, wired into the park controller.
**Non-Goals:** validation rules, commands, file persistence, migrating email references.

## Decisions

- **`business.model.User`**: private fields. `dni` is `final` and has no setter. The others have getters and setters. The constructor takes all seven values. `getPassword()` returns the stored (protected) value, and callers create users with `PasswordService.protect(raw)`. There is no persistence logic in `User`.
- **`business.controller.UserRegistry implements UserDirectory`**: an in-memory `LinkedHashMap<String, User>` by email, with `save(User)`, `findByEmail`, `findByDni`, `remove(email)` and `all()`. `dniOf(email)` adapts it to the port. It is not a singleton. It is created and injected by `AppConfiguration` (`park.setUserDirectory(registry)`) and exposed in `Configured` for the user milestones. File persistence (F1-C04) will add a repository behind it.
- **Uniqueness and validation** are not enforced here, because they belong to F1-C02/F1-C03. `save` replaces a user with the same email.

## Risks / Trade-offs

- [No command can create users yet] → USER CREATE arrives in F1-C03. Tests populate the registry directly.
