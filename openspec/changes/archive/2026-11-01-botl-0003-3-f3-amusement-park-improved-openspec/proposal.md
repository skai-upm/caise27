# Proposal

## Why

Deliveries 1–2 produced a working park application whose classes all live in one package, with managers created ad hoc, plaintext passwords, one attraction kind and no financial reporting. Delivery 3 (F3) must strengthen architecture (layers, Singleton controllers, dependency injection), security (BCrypt), extensibility (pluggable help formats, Builder-based invoices) and the domain (personal passes, typed attractions with pass-based entry) while every E1/E2 command keeps working. It is planned as one change implemented as ten ordered milestones (F3-C01…F3-C10), each closed by its mandatory commit.

## What Changes

- **F3-C01 Layers**: code moves to `presentation`, `business` (model, controllers, repository interfaces) and `persistence` (file repositories) packages; no default package; presentation never touches files; domain objects never do I/O.
- **F3-C02 Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` assembled step by step through an invoice Builder; only non-empty sections; no double counting of group-owned tickets; not persisted.
- **F3-C03 Controller lifecycle**: unique controllers become Singletons initialized once per execution; repositories (and the password hasher, invoice-builder factory) are created at startup and injected; normal/`--fly` differ only in repository paths.
- **F3-C04 Help**: `HELP` keeps concise syntax; new `HELP EXTENDED` adds descriptions; both render a shared command registry through an injected formatter.
- **F3-C05 Passwords**: BCrypt Maven dependency; passwords validated (E1 rules) then stored as BCrypt hashes; `USER DELETE`/`USER UPDATE` verify against the hash; a password change stores a new hash. The executable JAR bundles the dependency.
- **F3-C06 Personal passes**: `SPEEDPASS CREATE` (quantity 1 or 10, 20% of the ticket price each) and `SPECIALPASS CREATE` (one per ticket lifetime, ticket price); `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refund unused passes under E2 refund dates; consumed passes stay recorded; user invoices list passes.
- **F3-C07 Typed attractions**: `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`; access capabilities as interfaces, attractions as a bounded generic hierarchy; `ATTRACTION CREATE` gains an optional type (default `GENERAL`).
- **F3-C08 Pass-based entry**: `ATTRACTION ENTER;<atr>;<entrada>[;SPEED]` consumes SpeedPasses (one per admitted ticket, atomic for groups); SPECIAL attractions consume the ticket's Special Pass automatically and reject `SPEED`; consumed passes are reported.
- **F3-C09 Persistence & UML**: passes (price, ticket, state) and attraction types persist; ticket/group-ticket deletion cascades passes with an atomic file write; `--fly` covers the new data; `uml/model.puml` updated.
- **F3-C10 Integration**: all commands in one processor, `HELP`/`HELP EXTENDED` complete, interactive/file/`--fly` verified, executable JAR.
- Out of scope: GUI/HTTP, databases, DI frameworks, automatic migration of legacy plaintext passwords, new commercial rules.

## Capabilities

### New Capabilities
- `layered-application`: Package layering and responsibility boundaries between presentation, business and persistence.
- `controller-lifecycle`: Singleton controllers per execution with startup-configured, injected repositories and collaborators.
- `invoice-generation`: On-demand user and group invoices built with a Builder.
- `password-security`: BCrypt protection and verification of user passwords.
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, consumption state and refunds.

### Modified Capabilities
- `command-execution`: `HELP` driven by a command registry and injected formatter; `HELP EXTENDED` added.
- `attraction-management`: attraction types on creation, type-dependent admission with SPEED / Special Pass consumption, persisted type.
- `individual-tickets`: `TICKET DELETE` cascades the ticket's passes.
- `group-tickets`: `GROUPTICKET DELETE` cascades constituent passes, constituents, then the group ticket atomically.
- `user-persistence`: the stored password field holds a BCrypt hash.
- `project-build`: BCrypt allowed as runtime dependency; the executable JAR bundles runtime dependencies.
- `uml-documentation`: diagram covers interfaces, generics and the F3 classes.

## Impact

- All classes move from `es.upm.amusementpark` to `es.upm.amusementpark.{presentation,business.model,business.controller,business.repository,persistence}`; tests move accordingly; JAR main class becomes `es.upm.amusementpark.presentation.App`.
- New dependency `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` produces a self-contained `target/parque.jar`.
- File formats: `users.txt` password column holds hashes (legacy plaintext values still verify, not migrated); `tickets.txt` gains pass lines (`S`, `X`) and a special-pass-acquired flag; `attractions.txt` gains a type column (3-column lines read as `GENERAL`).
- Ten local milestone commits with the exact mandated messages, followed by the batch commit.
