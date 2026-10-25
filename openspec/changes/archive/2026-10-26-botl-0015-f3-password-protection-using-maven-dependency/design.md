# Design

## Context

`UserManager` stores `User` objects whose `password` field is written verbatim by `TextFileUserRepository`; `authenticate` compares strings. `Bootstrap` initialises the controllers. The JAR is built by `maven-jar-plugin` with a `Main-Class` manifest and no dependencies.

## Goals / Non-Goals

**Goals:**
- BCrypt hashing behind an abstraction injected into `UserManager`.
- A self-contained executable JAR.

**Non-Goals:**
- Changing file formats other than the password column content; encrypting other fields.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` (compile scope), the de-facto simple BCrypt implementation.
- **Abstraction**: `business.security.PasswordHasher` (`hash(String)`, `matches(String plain, String hash)`, `isHash(String)`) implemented by `BCryptPasswordHasher(int logRounds)`; `Bootstrap` uses cost 10, the `Park` test fixture cost 4 for speed (via a `Bootstrap.configure` overload accepting the hasher).
- **`UserManager.initialize(UserRepository, PasswordHasher)`**: `create` validates the plaintext format, then stores `hasher.hash(...)`; `authenticate` uses `hasher.matches`; `update` of `PASSWORD` validates the plaintext and stores a new hash; after loading, any non-hash password (`!isHash`) is hashed and the file saved (migration).
- **`isHash`**: BCrypt hashes are 60 characters starting with `$2a$`, `$2b$` or `$2y$`.
- **Packaging**: `maven-shade-plugin` 3.6.0 bound to `package` with a `ManifestResourceTransformer` (`Main-Class es.upm.parque.App`), producing `target/parque.jar` with jBCrypt classes inside; signature files filtered.
- **Package `business.security`** is under business (it holds a domain rule service); layering rules are unchanged.

## Risks / Trade-offs

- [BCrypt is slow by design] → Low cost factor in tests; cost 10 at runtime is negligible for interactive use.
- [Hash in `User.password` makes the getter return a hash] → Intended; no code shows passwords.
