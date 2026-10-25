# Proposal

## Why

Delivery 3 of the Amusement Park consolidates the code base and extends the business. The flat package from Deliveries 1–2 must be reorganised into presentation, business and persistence layers. Controllers must be explicit single instances with dependencies injected at startup (so normal and `--fly` runs differ only in configuration). Invoices must be generated with the Builder pattern, help must gain an extended mode, and passwords must stop being stored in plaintext. New SpeedPass/Special Pass products, typed attractions with generic access rules, and pass-based entry need full persistence and an updated UML model. The proposal names ten mandatory commits ([F3-C01] … [F3-C10]).

## What Changes

- **[F3-C01]** Move code into `es.upm.parque.presentation`, `es.upm.parque.business` (`model`, `controller`, `invoice`, `validation`, `security`) and `es.upm.parque.persistence`; command syntax and behavior are unchanged.
- **[F3-C02]** `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` build on-demand (not persisted) invoices with a Builder abstraction: user invoices list directly bought individual tickets and personal passes (including passes on group-ticket internal tickets, without counting those internal tickets again); group invoices list group tickets. Item prices and a total are shown.
- **[F3-C03]** Conceptually unique controllers become Singletons; repositories are plain objects created at startup with their paths and injected; controller collaborations are wired during initialisation.
- **[F3-C04]** `HELP EXTENDED` shows syntax plus a description per command; help text is produced by a help generator that receives its formatting behavior through dependency injection and works from the registered command list.
- **[F3-C05]** Passwords are stored as BCrypt hashes (`org.mindrot:jbcrypt` Maven dependency); USER DELETE/UPDATE verify against the hash; E1 password format rules still apply before hashing; legacy plaintext entries are migrated to hashes on load.
- **[F3-C06]** `SPEEDPASS CREATE;<entrada>;<1|10>`, `SPECIALPASS CREATE;<entrada>`, `SPEEDPASS DELETE;<idPase>`, `SPECIALPASS DELETE;<idPase>`: SpeedPasses cost 20 % of the ticket's stored price, a Special Pass costs the ticket price (max one per ticket); unused passes can be refunded under the E2 date rule; consumed passes stay.
- **[F3-C07]** Attraction types GENERAL, INDIVIDUAL, GROUP, SPECIAL using access interfaces and bounded generics; `ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` (default GENERAL).
- **[F3-C08]** `ATTRACTION ENTER;<atraccion>;<entrada>[;SPEED]`: SpeedPass consumption (one per member, atomic), automatic Special Pass consumption at SPECIAL attractions, SPEED rejected there; output reports consumed passes.
- **[F3-C09]** Persist passes (price, ticket, consumed state) and attraction types; cascade-delete passes with tickets/group tickets without partial deletions; `--fly` covers the new data; `uml/model.puml` updated.
- **[F3-C10]** Integration: `mvn package` builds an executable JAR bundling the BCrypt dependency; all modes and E1–E3 commands work; HELP/HELP EXTENDED list everything.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering rules for presentation, business and persistence.
- `component-wiring`: Singleton controllers and startup-time dependency injection of repositories and collaborators.
- `invoicing`: On-demand user and group invoices built step by step.
- `password-protection`: Hashed password storage and verification.
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, refund, persistence and cascade deletion.

### Modified Capabilities
- `attractions`: Typed attractions, optional type on creation, type-based admission, SpeedPass and Special Pass entry.
- `command-processing`: HELP lists the new commands; new HELP EXTENDED command.
- `ticketing`: Ticket and group-ticket deletion cascade to passes atomically.
- `project-build`: One external runtime dependency (BCrypt) is allowed and bundled into the executable JAR.
- `user-persistence`: The stored password field holds a hash instead of plaintext.
- `design-model`: The diagram must also document interfaces and generic attraction types of the E3 design.

## Impact

- All classes move packages (tests follow). Main class becomes `es.upm.parque.App` (unchanged FQN, now only a bootstrap).
- `pom.xml`: `org.mindrot:jbcrypt:0.4` runtime dependency, `maven-shade-plugin` to produce the executable fat JAR `target/parque.jar`.
- New data file `passes.txt`; `attractions.txt` gains a type column (old 3-column lines read as GENERAL); `users.txt` passwords become hashes.
- `uml/model.puml` rewritten for the E3 design.
