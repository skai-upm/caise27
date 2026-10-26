# Design

## Context

`UserController` validates passwords with `UserValidator.isValidPassword`, stores them as given in `User.password`, and `authenticate` compares strings. `UserRepository` writes the password column verbatim. The JAR is a plain jar with a `Main-Class` manifest and no dependencies. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Only hashes at rest; verification with BCrypt.
- Runnable fat JAR.

**Non-Goals:**
- Changing the password format rules; encrypting other fields.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` (single class, no transitive dependencies).
- **`PasswordHasher`** (`business.security`): wraps BCrypt with a configurable cost: `hash(plain)`, `matches(plain, hash)`, `isHash(value)` (BCrypt prefix `$2a$`/`$2b$`/`$2y$` and length 60). Injected into `UserController.configure(repository, hasher)` (startup DI, consistent with C03). `App` uses cost 10; tests use the minimum cost 4 for speed.
- **UserController**: create → validate → `hasher.hash`; PASSWORD update → validate → hash; `authenticate` → `hasher.matches`. On `configure`, any stored password that is not a hash is hashed and the file saved once (migration of pre-C05 data).
- **Repository unchanged**: it stores whatever `User.password` holds, which is now always a hash.
- **Fat JAR**: `maven-shade-plugin` 3.6.0 bound to `package`, with `ManifestResourceTransformer` (`es.upm.parque.App`); the shaded jar replaces `target/parque.jar`.

## Risks / Trade-offs

- [BCrypt is slow by design] → cost 10 for real runs (~tens of ms per hash); tests inject cost 4.
- [Plaintext migration on load] → happens once; a value that coincidentally looks like a hash would not be re-hashed (acceptable).
