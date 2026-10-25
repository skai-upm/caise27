# Proposal

## Why

Delivery 3 of the Amusement Park assignment completes the system: it adds typed attractions with SpeedPass / Special Pass access, invoices, extended help and protected passwords, and requires the code to demonstrate layered packages, exception-based error handling, singleton controllers with hand-written dependency injection, the Builder pattern and bounded generics. Everything from Deliveries 1 and 2 must keep working unless explicitly changed.

## What Changes

- Reorganise packages into `presentation`, `business` (model, controllers, invoices, exceptions, security, validation) and `persistence`; the presentation layer never touches files.
- Replace ad-hoc error handling with an exception hierarchy handled at the command-processing level.
- Split the central management into singleton controllers (system settings, users, groups, tickets, passes, attractions, invoices) coordinated by the existing singleton `ParkController`; repositories are plain (non-singleton) objects created at start-up and injected, so normal and `--fly` modes only differ in configuration.
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` built with the Builder pattern (sections only when they have content; group-ticket components never double-counted; passes only on user invoices).
- Add `HELP EXTENDED` (syntax plus purpose); help text is produced by a generator that receives its formatting behaviour by injection.
- **BREAKING**: passwords are stored as BCrypt hashes (jBCrypt 0.4 via Maven, bundled into the executable JAR); plaintext user files from earlier deliveries are not migrated.
- Add SpeedPasses (`SPEEDPASS CREATE;<purchaseId>;<ticket>;<1|10>`, ids `<purchaseId>:<n>`, 20 % of the ticket price each) and Special Passes (`SPECIALPASS CREATE;<id>;<ticket>`, at most one per ticket, price of the ticket), refunds (`SPEEDPASS DELETE`, `SPECIALPASS DELETE`), and cascade deletion of passes when their ticket or group ticket is deleted.
- Add attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` (`ATTRACTION CREATE;…;<type>`, default `GENERAL`), modelled with ticket access interfaces and bounded generics.
- Extend `ATTRACTION ENTER` with an optional `;SPEED` (consume one SpeedPass per entering ticket, atomically for groups) and automatic Special Pass consumption at special attractions.
- Persist attraction types and passes (price, ticket, consumed state); extend `HELP` and the PlantUML model.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, identifiers, refunds, consumption records and cascade deletion with tickets.
- `invoicing`: On-demand user and group invoices and their content rules.

### Modified Capabilities
- `application-execution`: Command set, `HELP` syntax list, new `HELP EXTENDED`, and the executable JAR now bundles the BCrypt dependency.
- `attractions`: Attraction creation takes an optional type; entry rules depend on attraction type and SpeedPass / Special Pass use.
- `ticketing`: Ticket and group ticket deletion also delete the associated passes.
- `park-persistence`: Persisted state now includes attraction types and passes; untyped attraction records load as `GENERAL`.
- `user-persistence`: The user file stores a BCrypt hash instead of the plain password.

## Impact

- All Java packages move (`es.upm.etsisi.park.{presentation,business,persistence}`); tests move accordingly.
- `pom.xml`: new `org.mindrot:jbcrypt:0.4` dependency and `maven-shade-plugin` so `target/amusement-park.jar` stays self-contained.
- New data file `passes.txt`; `attractions.txt` gains a type column; `users.txt` stores hashes.
- `uml/model.puml` and `README.md` updated.
- Out of scope: the course's mandatory per-milestone commit roadmap (`[F3-C01]` … `[F3-C10]`); the batch records this delivery as one traceable commit and mirrors the milestones in `tasks.md`.
