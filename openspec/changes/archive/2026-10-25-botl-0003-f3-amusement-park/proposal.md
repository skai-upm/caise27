# Proposal

## Why

After Delivery 2 the application works but lives in a single flat package, its main controller builds its own collaborators, passwords are stored in plaintext and attractions cannot differentiate ticket types. Delivery 3 (F3) asks for a layered, pattern-based design (Builder, Singleton, dependency injection, strategy-style help generation, interfaces and bounded generics) and new business features (invoices, hashed passwords, personal passes, typed attractions and pass-aware entry) while keeping every E1/E2 command and execution mode working.

## What Changes

- **[F3-C01] Layered packages**: reorganize the code into `presentation`, `business` (model, controllers, invoices, validation, security) and `persistence` packages, with the entry point and composition root in `es.upm.parque`. Command syntax and behavior stay unchanged; commands never touch files and domain objects never read or write files.
- **[F3-C02] Invoices with Builder**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` build an invoice step by step through a Builder abstraction and a director, including only applicable non-empty sections, item prices and a total. Internal tickets of a group ticket are never counted twice. Invoices are not persisted.
- **[F3-C03] Singleton controllers and dependency injection**: conceptually unique controllers become Singletons; repositories (behind interfaces) are created at startup with normal or `--fly` paths and injected, and controller-to-controller dependencies are configured during initialization.
- **[F3-C04] Extended help**: `HELP` keeps the syntax list; `HELP EXTENDED` adds a description per command. Both come from the registry of registered commands, and the help text generator receives its formatting behavior by injection.
- **[F3-C05] Password security**: add the jBCrypt Maven dependency; passwords are stored as BCrypt hashes (legacy plaintext entries are hashed on load), USER DELETE/UPDATE verify against the hash, and the E1 format rules still apply before hashing. **BREAKING** for the users file format (password column now holds a hash).
- **[F3-C06] Personal passes**: `SPEEDPASS CREATE;IdEntrada;Cantidad` (1 or 10 independent SpeedPasses at 20 % of the ticket price), `SPECIALPASS CREATE;IdEntrada` (at most one per individual ticket over its lifetime, at the ticket price), `SPEEDPASS DELETE;IdPase` and `SPECIALPASS DELETE;IdPase` refunds for unused passes of future tickets. Consumed passes remain.
- **[F3-C07] Typed attractions with interfaces and bounded generics**: GENERAL, INDIVIDUAL, GROUP and SPECIAL attraction types; `ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` (E2 syntax creates GENERAL); access capability interfaces and `Attraction<T extends AccessCapability>` restrict admitted tickets.
- **[F3-C08] Pass-aware entry**: `ATTRACTION ENTER;A;T;SPEED` consumes one SpeedPass per entering user (atomic for groups); SPECIAL attractions automatically consume the ticket's Special Pass and reject SPEED; failed entries consume nothing; output reports consumed passes.
- **[F3-C09] Persistence and UML**: passes (price, ticket, consumed status) and attraction types persist; ticket and group-ticket deletion cascade to passes without partial deletions; `--fly` covers the new files; `uml/model.puml` reflects the E3 design.
- **[F3-C10] Integration**: all E1, E2 and E3 commands work together in interactive, file and `--fly` modes; `mvn package` produces the executable JAR; HELP/HELP EXTENDED list everything.

## Capabilities

### New Capabilities

- `layered-architecture`: Package layering rules for presentation, business and persistence.
- `invoices`: On-demand user and group invoices assembled with a Builder.
- `controller-lifecycle`: Singleton controllers and startup-time dependency injection of repositories and collaborators.
- `password-security`: BCrypt hashing and verification of user passwords.
- `personal-passes`: SpeedPasses and Special Passes: purchase, pricing, refunds, consumption status and persistence.

### Modified Capabilities

- `command-processing`: HELP lists the new commands; HELP EXTENDED added; help generation receives its format by injection.
- `attractions`: Attraction types, typed creation, type-restricted and pass-aware entry, persisted type.
- `ticket-management`: Ticket deletion cascades to its passes.
- `group-tickets`: Group-ticket deletion cascades to passes, then internal tickets, then the group ticket, without partial deletions.
- `user-persistence`: The password column stores a BCrypt hash.
- `project-build`: A runtime dependency (jBCrypt) is now allowed; the main class moves with the reorganization.
- `temporary-execution`: `--fly` also covers pass data.
- `uml-model`: The diagram must represent interfaces and generic types of the E3 design.

## Impact

- Every class moves to a new package; tests move accordingly. `App` stays at `es.upm.parque.App` (manifest unchanged).
- New dependency `org.mindrot:jbcrypt:0.4`.
- New data file `passes.txt`; `tickets.txt` gains a Special-Pass-purchased flag (old 5-field lines still load); `attractions.txt` gains a type column (old 3-field lines load as GENERAL); `users.txt` passwords become hashes.
- `uml/model.puml` rewritten for the E3 design.
