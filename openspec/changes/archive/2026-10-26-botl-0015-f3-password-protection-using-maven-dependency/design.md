# Design

## Context

`UserController` compares plaintext passwords (`user.getPassword().equals(password)`) and stores them as given; `FileUserRepository` writes them in the `users.txt` password column. Controllers are singletons configured in `App.initialize` (dependency injection by `configure`). The JAR is built by `maven-jar-plugin` with no dependencies.

## Goals / Non-Goals

**Goals:** BCrypt hashes for all stored passwords; verification against hashes; external library through Maven; JAR still self-contained.

**Non-Goals:** salting/pepper policies beyond BCrypt defaults, password reset.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` — small, dependency-free, widely used. Alternative: Spring Security Crypto — heavier and pulls other artifacts.
- **Abstraction** in `es.upm.parque.business.security`: `PasswordEncoder` with `hash(String)`, `matches(String raw, String hash)`, `isHash(String)`. `BCryptPasswordEncoder(int cost)` implements it (default cost 10; tests use cost 4 for speed). Injected via `UserController.configure(UserRepository, PasswordEncoder)`.
- **Flow**: validation (E1 rules) runs on the raw password first; only valid passwords are hashed. `authenticate` uses `encoder.matches`. `update(PASSWORD)` stores `encoder.hash(newValue)`.
- **Legacy migration**: in `configure`, each loaded user whose password is not a hash (`!encoder.isHash(...)`, i.e. not a `$2a$/$2b$/$2y$` BCrypt string) is re-hashed, and the list is saved once if anything changed.
- **Repository unchanged**: it persists whatever the user holds (now a hash). BCrypt output uses `[./A-Za-z0-9$]`, so it never contains the `;` separator.
- **Fat JAR**: `maven-shade-plugin` 3.6.0 at `package` with a `ManifestResourceTransformer` (main class) and `createDependencyReducedPom=false`; the shaded artifact replaces `target/parque.jar`.
- **Spec cleanup**: remove the obsolete project-build requirement "No domain functionality yet".

## Risks / Trade-offs

- [BCrypt is slow by design] → cost 10 in the application; tests inject cost 4.
- [Existing `data/users.txt` with plaintext] → migrated automatically on the next start.
