# Design

## Decisions

- **Registry in `ParkController`**: a `LinkedHashMap<String, User>` keyed by e-mail. A DNI uniqueness check traverses its values (there are few users, and this avoids two maps drifting apart). `registerUser` raises `ParkException` with `User with DNI X already exists` or `User with email X already exists`.
- **Order of checks**: format validation first, then uniqueness, then protection. Hashing is costly, so it runs last and only for accepted data.
- **Default directory**: `registeredUsers` is a `UserDirectory` that reads the registry (`exists` and `dniOf`). `reset()` restores it, and the setter still allows test stubs. `findUser(email)` and `getUsers()` are exposed for upcoming operations (deletion, update, persistence).
- **`presentation.UserCommands`**: `USER CREATE` uses `CommandSupport.fields(line, 8, usage)`. Errors flow through the central exception handling.
