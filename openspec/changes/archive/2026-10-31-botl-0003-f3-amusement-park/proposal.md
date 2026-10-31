# Proposal

## Why

After Deliveries 1 and 2 the application supports users, groups, tickets, attractions, command processing and normal/temporary persistence, but all code lives in one flat package, controllers are freely instantiated, passwords are stored in plaintext and attractions treat every ticket alike. Delivery 3 (F3) strengthens architecture, security and extensibility and adds invoices, personal passes and typed attractions, delivered as one coherent change in ten ordered milestones (F3-C01 … F3-C10), each closed by its mandatory commit.

## What Changes

- **F3-C01 Layers**: code moves into `presentation`, `business` (model, controllers, rules, …) and `persistence` packages; no behavior change.
- **F3-C02 Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` built step by step with a Builder; only applicable sections; no persistence.
- **F3-C03 Lifecycle**: unique controllers become Singletons initialised once per execution; repositories become interfaces with file implementations created at startup and injected (normal vs `--fly` differ only in paths).
- **F3-C04 Help**: `HELP` keeps concise syntax; new `HELP EXTENDED` adds descriptions; both rendered from a command registry by an injected formatter.
- **F3-C05 Passwords (modifies user management)**: BCrypt Maven dependency; passwords validated with E1 rules, then stored as BCrypt hashes; authentication verifies against the hash.
- **F3-C06 Personal passes**: `SPEEDPASS CREATE` (quantity 1 or 10, 20% of ticket price each), `SPECIALPASS CREATE` (one per ticket lifetime, ticket price), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds of unused passes on future tickets; persisted; included in user invoices.
- **F3-C07 Attraction types (modifies attractions)**: `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` via access-capability interfaces and bounded generics; E2 creation syntax still creates `GENERAL`; optional type parameter.
- **F3-C08 Pass-based entry (modifies attractions)**: `ATTRACTION ENTER;<a>;<t>;SPEED` consumes SpeedPasses (one per admitted user, atomic for groups); SPECIAL attractions consume the ticket's Special Pass automatically and reject `SPEED`.
- **F3-C09 Persistence and UML (modifies tickets, group tickets, temporary persistence, UML)**: cascade deletion of passes with tickets and group tickets, staged multi-file writes so failures leave no partial state, attraction type persisted, `--fly` covers new files, `uml/model.puml` updated.
- **F3-C10 Integration**: all E1/E2/F3 commands in one processor, listed by both help modes; normal, file and `--fly` modes verified with the packaged JAR.

Out of scope: GUI/HTTP, databases, DI frameworks, automatic migration of legacy plaintext passwords, commercial rules not in the source.

## Capabilities

### New Capabilities
- `application-architecture`: Layered packages, Singleton controllers and startup dependency injection of replaceable repositories.
- `invoicing`: On-demand user and group invoices assembled with a Builder.
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, persistence, consumption state and refunds.

### Modified Capabilities
- `project-build`: the BCrypt library becomes the one allowed runtime dependency and the executable JAR bundles it.
- `command-execution`: `HELP` driven by a command registry; new `HELP EXTENDED`.
- `user-management`: passwords stored as BCrypt hashes and verified against them.
- `attraction-management`: attraction types, typed creation, SPEED and Special Pass entry, persisted type.
- `individual-tickets`: `TICKET DELETE` cascades to the ticket's passes.
- `group-tickets`: `GROUPTICKET DELETE` cascades to constituent passes and tickets atomically.
- `temporary-persistence`: `--fly` isolates pass data too.
- `uml-documentation`: diagram covers packages, interfaces and generics.

## Impact

- All Java sources and tests move to `es.upm.amusementpark.{presentation,business.*,persistence}`.
- New dependency `org.mindrot:jbcrypt:0.4` (runtime) — the JAR is packaged with its dependency so `java -jar` keeps working.
- New data file `passes.txt`; `attractions.txt` gains an optional type column and `tickets.txt` an optional Special-Pass-issued column (old files still load).
- Existing plaintext passwords in `users.txt` keep working (verified as legacy plaintext) until changed; no forced migration.
- Ten local milestone commits with the exact source messages precede the BOTL archive commit.
