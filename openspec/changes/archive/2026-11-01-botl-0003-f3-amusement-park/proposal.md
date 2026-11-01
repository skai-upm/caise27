# Proposal

## Why

The F1/F2 application works but keeps every class in one package, stores passwords in plaintext, has a single all-purpose controller, and lacks the Delivery 3 commercial features. Delivery 3 (F3) strengthens architecture (layers, Singleton controllers, dependency injection), security (BCrypt), extensibility (injected help formats, Builder invoices, generic attraction access), and the domain (SpeedPass and Special Pass) without breaking any E1/E2 command or execution mode.

## What Changes

Delivered as one change, implemented in the source milestone order F3-C01 … F3-C10, each closed by its mandatory commit message:

- **F3-C01 Layered packages**: code moves to `presentation`, `business` (model, controllers, rules) and `persistence` (repositories, file access) packages; behaviour unchanged.
- **F3-C02 Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` built with a Builder, including only applicable sections and a total; not persisted.
- **F3-C03 Controllers and DI**: unique controllers (system settings, users, park, invoices) become Singletons; repositories become replaceable interfaces with file implementations created at startup and injected; normal and `--fly` modes differ only in configured paths.
- **F3-C04 Extended help**: `HELP` stays concise; `HELP EXTENDED` adds descriptions; both generated from one command registry by a help generator that receives its formatting behaviour by injection.
- **F3-C05 BCrypt**: `jbcrypt` dependency; new and changed passwords are stored as BCrypt hashes after E1 format validation; authentication verifies against the stored value.
- **F3-C06 Passes**: `SPEEDPASS CREATE` (quantity 1 or 10, 20% of the ticket price each), `SPECIALPASS CREATE` (one per individual ticket for its lifetime, ticket price), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds of unused passes under E2 date rules; user invoices gain a passes section.
- **F3-C07 Attraction types**: `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` attractions using access-capability interfaces and bounded generics; E2 creation syntax creates `GENERAL`, an optional 5th parameter sets the type.
- **F3-C08 Pass-based entry**: optional `;SPEED` on `ATTRACTION ENTER` consumes one SpeedPass per admitted user (atomic for groups); `SPECIAL` attractions consume a Special Pass automatically and reject `SPEED`; outputs report consumed passes.
- **F3-C09 Persistence and UML**: passes (price, ticket, consumed state, special-pass history) and attraction types persist; ticket deletions cascade to passes with atomic file replacement; `uml/model.puml` reflects the F3 code.
- **F3-C10 Integration**: all commands coexist; `HELP`/`HELP EXTENDED` list them; normal, file and `--fly` modes verified end to end; executable JAR bundles the BCrypt library.

Out of scope: GUI/HTTP interfaces, databases, DI frameworks, automatic migration of existing plaintext password files, new commercial rules beyond the source.

## Capabilities

### New Capabilities
- `layered-architecture`: Package separation of presentation, business, and persistence responsibilities.
- `invoice-generation`: On-demand user and group invoices assembled with a Builder.
- `controller-lifecycle`: Singleton controllers and startup-time dependency injection of repositories and collaborators.
- `password-security`: BCrypt protection and verification of user passwords.
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, consumption state, and refund.
- `attraction-ticket-typing`: Attraction types and the ticket kinds each admits, enforced through interfaces and bounded generics.
- `pass-based-entry`: SpeedPass and Special Pass use when entering attractions, with atomic consumption.

### Modified Capabilities
- `command-execution`: `HELP` lists F3 commands; `HELP EXTENDED` added.
- `user-persistence`: the stored password field holds a protected (hashed) value.
- `individual-tickets`: `TICKET DELETE` cascades to passes; ticket persistence includes passes.
- `group-tickets`: `GROUPTICKET DELETE` cascades to constituent passes atomically.
- `attraction-management`: creation accepts a type; entry/exit accept constituent ticket identifiers and type rules; type persists.
- `uml-documentation`: the diagram includes F3 interfaces, generics, and packages.
- `temporary-persistence`: fly storage also isolates pass and attraction-type data.

## Impact

- All sources move to `es.upm.amusementpark.{presentation,business.*,persistence}`; tests follow.
- New runtime dependency `org.mindrot:jbcrypt:0.4`, bundled into `target/parque.jar` with `maven-shade-plugin`.
- `tickets.txt` gains pass records and `attractions.txt` a type column; older files still load (missing type → `GENERAL`, no passes).
- Existing plaintext passwords in `users.txt` remain verifiable (no silent migration) and are replaced by a hash when the password is changed.
- Ten milestone commits with the exact source messages are created during implementation.
