# Proposal

## Why

Delivery 3 (F3) matures the Amusement Park application. The flat single package from Deliveries 1–2 must become a layered design (presentation / business / persistence) with singleton controllers and injected repositories. On top of that, F3 adds invoices built with the Builder pattern, extended help with an injected formatting strategy, BCrypt-protected passwords (the first external Maven dependency), personal passes (SpeedPass and Special Pass), attraction types enforced with interfaces and bounded generics, pass consumption on entry, cascade-deleting persistence, and an updated UML model, all while keeping every E1/E2 command and execution mode working.

## What Changes

- **[F3-C01]** Reorganise code into packages `es.upm.parque.presentation`, `es.upm.parque.business` (model, controllers, invoices, validation, repository interfaces, security) and `es.upm.parque.persistence` (file repositories), with `es.upm.parque` holding only the bootstrap. Command syntax and behavior unchanged.
- **[F3-C02]** Add `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>`, assembled step by step through a Builder abstraction; invoices are generated on demand and not persisted.
- **[F3-C03]** Controllers become Singletons; repositories are plain objects created at startup (normal or `--fly` paths) and injected, together with controller-to-controller dependencies, during initialisation.
- **[F3-C04]** Keep `HELP` (syntax) and add `HELP EXTENDED` (syntax + description), both generated from the registered commands by a help generator that receives its formatting strategy by injection.
- **[F3-C05]** Store passwords as BCrypt hashes (new Maven dependency); authentication verifies against the hash; E1 format rules still apply before hashing; legacy plaintext passwords are hashed on load. The executable JAR bundles the dependency.
- **[F3-C06]** Add personal passes for individual tickets: `SPEEDPASS CREATE;<ticket>;<1|10>`, `SPEEDPASS DELETE;<speedPassId>`, `SPECIALPASS CREATE;<ticket>`, `SPECIALPASS DELETE;<ticket>`, with prices (20% / 100% of the ticket price), consumption status and refunds of unused passes before the ticket date.
- **[F3-C07]** Add attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL using an access-capability interface and bounded generics; `ATTRACTION CREATE;id;name;capacity` still creates GENERAL, and an optional fourth field selects the type.
- **[F3-C08]** Extend `ATTRACTION ENTER;<atraccion>;<entrada>[;SPEED]` to consume SpeedPasses (one per admitted ticket, atomically) and, at SPECIAL attractions, the ticket's Special Pass.
- **[F3-C09]** Persist passes (price, ticket, consumption) and attraction types; `TICKET DELETE` / `GROUPTICKET DELETE` cascade to passes with no partial deletions; `--fly` covers the new files; update `uml/model.puml`.
- **[F3-C10]** Integrate everything: HELP/HELP EXTENDED list all commands; interactive, file and `--fly` modes keep working; `mvn package` builds the executable JAR.
- **BREAKING (storage format)**: the users file stores password hashes instead of plaintext (automatic migration on load).

## Capabilities

### New Capabilities
- `application-architecture`: Layered package structure, singleton controllers, and start-up configuration/injection of repositories and collaborators.
- `invoicing`: On-demand user and group invoices built with a Builder.
- `personal-passes`: SpeedPasses and Special Passes: purchase, pricing, refund, consumption state and persistence.

### Modified Capabilities
- `project-build`: Runtime dependencies are no longer limited to the standard library (BCrypt), and the executable JAR must include them.
- `command-processing`: HELP lists the new commands; HELP EXTENDED is added.
- `user-management`: Passwords are verified against stored hashes.
- `user-persistence`: The users file stores password hashes and migrates legacy plaintext passwords.
- `attractions`: Attraction types, optional type in creation, SPEED/Special Pass entry rules, and type persistence.
- `ticketing`: Deleting an individual ticket also deletes its passes.
- `group-ticketing`: Deleting a group ticket deletes the passes of its individual tickets, then those tickets, then the group ticket.
- `temporary-execution`: The temporary directory also holds pass data.
- `design-model`: The diagram also represents interfaces, generics and the new classes.

## Impact

- Every class moves to a new package (main class becomes `es.upm.parque.App` bootstrap with presentation/business/persistence packages); tests move accordingly.
- `pom.xml`: `org.mindrot:jbcrypt` dependency and `maven-shade-plugin` so `target/parque.jar` stays executable.
- New data file `passes.txt`; `attractions.txt` gains a type column (old lines read as GENERAL); `users.txt` holds BCrypt hashes.
- `uml/model.puml` rewritten for the E3 design.
