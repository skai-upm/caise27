# Design

## Context

There is no user model yet. Users are reached through the `UserDirectory` port (existence and DNI). The executable JAR is a plain `maven-jar-plugin` artifact with no dependencies, and the source now mandates a Maven BCrypt dependency (jBCrypt 0.4 is the suggested one).

## Goals / Non-Goals

**Goals:** a ready-to-use, injected password component, and a self-contained executable JAR.
**Non-Goals:** user storage, authentication commands, migration.

## Decisions

- **Package `es.upm.park.business.security`**:
  - `PasswordHasher` (port): `hash(raw)` and `matches(raw, hash)`.
  - `BCryptPasswordHasher` implements it with `BCrypt.hashpw(raw, BCrypt.gensalt())` and `BCrypt.checkpw`. `checkpw` throws `IllegalArgumentException` on malformed hashes, which is translated to "no match".
  - `PasswordPolicy` (functional interface, `validate(raw)` throws `InvalidInputException`). `PasswordPolicy.notBlank()` is the default.
  - `PasswordService(PasswordHasher, PasswordPolicy)` with `protect(raw)` (validate, then hash) and `verify(raw, stored)`.
  The adapter stays in the business layer: it is a thin wrapper over a pure algorithm library, not persistence, and the port keeps it replaceable.
- **Packaging**: `maven-shade-plugin` 3.6.0 at `package` builds an uber-JAR replacing `amusement-park.jar`, with a `ManifestResourceTransformer` for `es.upm.park.App` and `createDependencyReducedPom=false`. This was chosen over an `assembly` jar-with-dependencies, because shade keeps the existing JAR name and `java -jar` command unchanged.
- **Configuration**: `AppConfiguration` creates `PasswordService(new BCryptPasswordHasher(), PasswordPolicy.notBlank())` and exposes it in `Configured` for the user milestones.

## Risks / Trade-offs

- [BCrypt is slow by design (about 50-100 ms per hash at cost 10)] → Acceptable for interactive use. Tests hash only a few passwords.
- [The full Delivery 1 password rules are not applied yet] → The policy is injectable, and the valid-users milestone supplies it. This is noted in the proposal.
