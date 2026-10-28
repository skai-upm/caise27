# Design

## Context

`User` has final `dni`, final `email` and mutable `passwordHash`; the main class (`App`), controller and repositories are already separate classes. `TextUserRepository` writes `dni;email;passwordHash`.

## Decisions

- **`User`** fields: `firstName`, `surnames`, `dni` (final), `email`, `passwordHash`, `phone`, `card`. Full constructor `User(firstName, surnames, dni, email, passwordHash, phone, card)`; existing short constructors delegate with empty strings (kept for minimal records such as historical ticket holders). Setters for every attribute except DNI. E-mail becomes non-final so a later `USER UPDATE` can change it (the controller's e-mail index will be updated by that change).
- **Persistence**: `dni;email;passwordHash;firstName;surnames;phone;card`; 2, 3 or 7 fields accepted.
- **UML**: add the attributes to `User`.
