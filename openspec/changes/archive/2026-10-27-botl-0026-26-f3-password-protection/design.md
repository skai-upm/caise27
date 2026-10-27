# Design

## Decisions

- **`business.security.PasswordHasher`** (`hash(plain)`, `verify(plain, stored)`) with **`BCryptPasswordHasher(int logRounds)`** using `BCrypt.hashpw(plain, BCrypt.gensalt(logRounds))` and `BCrypt.checkpw`; a malformed stored value is treated as non-matching. Injected via `UserController.configure(users, park, hasher)` (consistent with F3-C03).
- `User.password` keeps holding the stored (protected) value; the user file format is unchanged, only the content differs.
- Validation order in USER CREATE/UPDATE is unchanged; hashing happens after all checks.
- **Packaging**: `maven-shade-plugin` bound to `package`, `createDependencyReducedPom=false`, with the `ManifestResourceTransformer` main class; the shaded JAR replaces `target/nombre-aplicacion.jar`.
- `ApplicationInitializer`: work factor 10 for file-backed executions, 4 for in-memory ones.
