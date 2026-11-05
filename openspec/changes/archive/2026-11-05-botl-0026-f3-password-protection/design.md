## Context

- `User.password` holds the plain password.
- `UserManager.create` and `update` validate it with `UserValidator.isValidPassword`.
- `authenticate` compares with `equals`.
- `UserRepository` loads `users.txt` and keeps only users for which `UserValidator.isValid(user)` holds, which currently includes `isValidPassword`.
- The project spec forbids regex APIs in main sources.
- The JAR is built by `maven-jar-plugin` with no dependencies bundled.
- Managers are singletons with dependencies injected by `ParkConfiguration` (F3-C03).

## Goals / Non-Goals

**Goals:**
- Use BCrypt through jBCrypt.
- Validate before protecting.
- Verify with the library.
- Generate a new value on every change.
- Keep the JAR self-contained.

**Non-Goals:**
- Migrating old files.
- Password policies beyond Delivery 1.
- Protecting other fields.

## Decisions

1. **Strategy plus injection.**
   - `PasswordProtector` interface in `business`.
   - `BCryptPasswordProtector(int cost)` uses `BCrypt.hashpw(plain, BCrypt.gensalt(cost))` and `BCrypt.checkpw`.
   - `matches` returns `false` when the stored value is not a valid BCrypt value: jBCrypt throws `IllegalArgumentException` for malformed salts, which is caught and treated as no match.
   - `UserManager.inject(ParkController, PasswordProtector)`.
   - `ParkConfiguration.initialize(repos)` uses `new BCryptPasswordProtector()` with cost 10. The overload `initialize(repos, protector)` lets tests inject cost 4 (`TestSupport`), which keeps the roughly 200-test suite fast. Production always uses the default.
2. **Validation order.** The plain password is checked with `isValidPassword` before protecting it, in both create and update. The rest of creation (DNI, email, card, uniqueness) is unchanged.
3. **Stored-format check (`UserValidator.isProtectedPassword`).**
   - The check is hand-written, without regex. Length is 60: `$`, then `2` plus one of `a`, `b` or `y`, then `$`, two digits, `$`, and 53 characters from `./A-Za-z0-9`.
   - `isValid(user)` uses it instead of `isValidPassword`, because `isValid` is only applied to stored users.
   - The `Whole-user validation` spec is modified accordingly.
4. **File format.** It is unchanged, because a BCrypt value has no `;` and goes in the same field.
5. **Packaging.**
   - `maven-shade-plugin` 3.6.0, bound to `package`, with `createDependencyReducedPom=false`.
   - The `ManifestResourceTransformer` sets `Main-Class`. The shaded JAR replaces `target/amusement-park.jar`.
   - `maven-jar-plugin` keeps its manifest configuration.
6. **Tests.**
   - Assertions of plain passwords in memory or on file become `BCrypt.checkpw` checks.
   - Load tests write lines with a precomputed BCrypt value.
   - A new `PasswordProtectionTest` covers:
     - file contents;
     - verification after restart;
     - the protected value rejected as a password;
     - a password change;
     - an invalid new password;
     - malformed stored values;
     - the jBCrypt class inside the JAR (checked via an end-to-end run).

## Risks / Trade-offs

- Users in existing `users.txt` files from earlier versions are ignored at start-up and reported in the warning. The source accepts this.
- BCrypt is intentionally slow: about 70 ms per hash at cost 10. That is negligible for interactive use.
