# Design

## Context

`UserController` validates the plaintext password (`UserValidator.isValidPassword`), stores it as is in `User.password`, and `authenticate` compares with `equals`. `UserFileStorage` writes the field unchanged. Controllers are singletons initialised by `App` with injected repositories. The JAR is a plain jar with a `Main-Class` manifest. See proposal.md.

## Goals / Non-Goals

**Goals:** only hashes stored; verification through the library's normal salt/check functions; abstraction injected like other dependencies; runnable JAR.

**Non-Goals:** other schemes, migration of old plaintext files, password policies beyond E1.

## Decisions

- **Library:** `org.mindrot:jbcrypt:0.4` (small, no transitive dependencies; `BCrypt.hashpw(pw, BCrypt.gensalt())` and `BCrypt.checkpw`).
- **Abstraction:** `business.security.PasswordHasher { String hash(String plain); boolean matches(String plain, String stored); }`; `BCryptPasswordHasher` implements it with a configurable log-rounds value (default 10, the library default). `matches` returns `false` for null/blank or non-BCrypt stored values (prefix `$2a$`/`$2b$`/`$2y$`, length 60) instead of letting the library throw.
- **Injection:** `UserController.initialize(UserRepository, PasswordHasher)`; `App` creates `new BCryptPasswordHasher()`. The `User` keeps the hash in its `password` field (model unchanged); persistence stores whatever the model holds, so the file contains the hash.
- **Order of operations:** validate plaintext with the E1 rules → hash → store. On `PASSWORD` update, validation happens before hashing and a new salt is used. Authentication = email lookup + `matches`.
- **Legacy plaintext records:** a stored value that is not a BCrypt hash never matches, so such users cannot authenticate (no silent migration). They must be re-created; this is reported, not hidden.
- **Packaging:** `maven-shade-plugin` (3.6.0) at `package` replaces the main artifact with a JAR including jbcrypt; the manifest main class stays `es.upm.etsisi.amusementpark.App`.
- **Tests** that asserted plaintext passwords in files check the hash with `BCrypt.checkpw` instead; tests may inject a hasher with lower cost to stay fast.

## Risks / Trade-offs

- [Hashing cost slows tests] → `BCryptPasswordHasher(int logRounds)` lets tests use the minimum cost (4).
- [Existing data with plaintext passwords] → accounts unusable until re-created; documented as BREAKING.
