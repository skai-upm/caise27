# Design

## Context

`UserManager` validates passwords with `UserValidator.isValidPassword`, stores the plaintext in `User.password`, and authenticates with `getPassword().equals(password)` (`indexOfCredentials`). Controllers are singletons configured by `ApplicationContext`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- Only hashes stored; same commands and messages; hashing replaceable and injected.

**Non-Goals:**
- Migrating existing plaintext files, other algorithms, password policies beyond E1.

## Decisions

- **Library**: `org.mindrot:jbcrypt:0.4` (small, dependency-free, standard `BCrypt.hashpw/checkpw/gensalt`). Compile scope so it is available to `mvn package`; since the packaged JAR is not shaded, running `java -jar` needs the library on the classpath — the manifest gets a `Class-Path` entry (`lib/jbcrypt-0.4.jar`) and `maven-dependency-plugin:copy-dependencies` copies it to `target/lib` during `package`, keeping the existing `java -jar target/<jar>` invocation working without a fat-JAR plugin.
- **Abstraction**: `business.PasswordHasher` (`hash(String)`, `matches(String plain, String stored)`), implemented by `business.BCryptPasswordHasher` (log rounds 10 by default, configurable through its constructor). `matches` returns false when the stored value is not a valid BCrypt hash (catches `IllegalArgumentException` from `checkpw`), giving controlled failure for legacy plaintext records.
- **Injection**: `UserManager.configure(UserRepository, PasswordHasher)`; `ApplicationContext` passes `new BCryptPasswordHasher()`. Tests can inject a hasher with lower cost to keep the suite fast.
- **Flow**: create → validate plaintext (unchanged order of checks) → `hash` → store; update PASSWORD → validate → `hash`; authentication → `matches`. `User.password` keeps its name and now always holds the hash (the user model never sees plaintext after creation).
- **Equivalence**: command outputs are unchanged; `users.txt` differs only in the password column (random salts), so the E1/E2 script is compared on output and on all data files except that column.

## Risks / Trade-offs

- [BCrypt cost slows tests] → tests that create many users inject a low-cost hasher where they build managers directly; end-to-end tests keep the production cost.
- [Old data files stop authenticating] → explicitly accepted by the source (no automatic migration); documented.
