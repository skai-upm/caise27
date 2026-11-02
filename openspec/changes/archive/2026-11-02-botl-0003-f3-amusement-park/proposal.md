# Proposal

## Why

Deliveries 1–2 produced a working park application (users, groups, tickets, attractions, `--fly`) in a single flat package with plaintext passwords. Delivery 3 (F3) must strengthen architecture (layers, Singleton controllers, dependency injection), security (BCrypt), and the domain (invoices, SpeedPass/Special Pass, typed attractions with pass-based entry), while keeping every E1/E2 command working. All ten F3 milestones (F3-C01…F3-C10) are delivered as one change, implemented in milestone order with the mandatory milestone commits.

## What Changes

- **F3-C01 Layers**: code moves into `com.amusementpark.presentation`, `com.amusementpark.business` (with `model` and `controller` subpackages), and `com.amusementpark.persistence`. No command syntax or behavior changes.
- **F3-C02 Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` built step by step by a Builder; not persisted.
- **F3-C03 Lifecycle**: conceptually unique controllers become Singletons; repositories become interfaces with file implementations created at startup (paths differ in `--fly`) and injected, as are controller collaborators.
- **F3-C04 Help**: `HELP` keeps concise syntax; new `HELP EXTENDED` adds descriptions; the help generator receives its formatter by injection and both read the command registry.
- **F3-C05 Passwords**: BCrypt Maven dependency; passwords are validated (E1 rules), then stored as BCrypt hashes; `USER DELETE`/`USER UPDATE` verify against the hash; password changes store a new hash. **BREAKING** for the users file content (password column holds hashes).
- **F3-C06 Passes**: `SPEEDPASS CREATE`/`DELETE` (quantity 1 or 10, 20% of ticket price each) and `SPECIALPASS CREATE`/`DELETE` (one per ticket ever, ticket price); consumed passes are kept; unused passes are refundable while their ticket is refundable.
- **F3-C07 Typed attractions**: `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`, implemented with access-capability interfaces and bounded generics; `ATTRACTION CREATE` without type still creates `GENERAL`.
- **F3-C08 Pass entry**: `ATTRACTION ENTER;<atraccion>;<entrada>[;SPEED]`; SPEED consumes one SpeedPass per admitted user atomically; SPECIAL attractions consume a Special Pass automatically and reject SPEED.
- **F3-C09 Persistence and UML**: passes, consumption state, special-pass history, and attraction type are persisted; ticket and group-ticket deletion cascade to passes without partial state; `--fly` isolates the new files; `uml/model.puml` updated.
- **F3-C10 Integration**: all commands in one processor, `HELP`/`HELP EXTENDED` complete, normal and `--fly` modes, executable `target/parque.jar` including the BCrypt dependency.

Out of scope: GUI/HTTP interfaces, databases, DI frameworks, automatic migration of existing plaintext password files, new commercial features.

## Capabilities

### New Capabilities
- `application-architecture`: Layered package structure, Singleton controllers, and startup-time dependency injection of repositories and collaborators.
- `invoices`: On-demand user and group invoices assembled with a Builder.
- `personal-passes`: SpeedPass and Special Pass purchase, refund, consumption state, and persistence.

### Modified Capabilities
- `command-execution`: `HELP` built from the command registry; new `HELP EXTENDED`.
- `user-management`: passwords protected with BCrypt and verified against the stored hash.
- `user-persistence`: the users file stores password hashes.
- `project-build`: BCrypt runtime dependency allowed; the executable JAR bundles runtime dependencies.
- `attraction-management`: attraction types, typed creation, type-dependent and pass-based entry, type in status and persistence.
- `individual-tickets`: ticket deletion cascades to its passes.
- `group-tickets`: group-ticket deletion cascades to constituent passes and tickets atomically.
- `temporary-persistence`: `--fly` also isolates pass data.
- `uml-documentation`: diagram covers interfaces, generic types, and the pass model.

## Impact

- All Java sources move to new packages; tests move accordingly; the manifest main class becomes `com.amusementpark.presentation.App`.
- New dependency `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` packages it into `target/parque.jar`.
- New/changed files: `passes.txt` (new), `tickets.txt` (extra special-pass column), `attractions.txt` (extra type column); E2 files without the new columns still load.
- Users files written by E1/E2 keep plaintext passwords until changed (see design D6); no automatic migration.
- Ten local milestone commits with the mandated messages precede the batch commit for this change.
