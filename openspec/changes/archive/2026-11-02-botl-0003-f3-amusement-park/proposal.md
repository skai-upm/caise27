# Proposal

## Why

Delivery 3 (milestones F3-C01 through F3-C10) evolves the Delivery 2 park system with explicit application layers, an exception-based error design, singleton controllers with injected repositories, BCrypt-protected passwords, invoices built with the Builder pattern, SpeedPasses and Special Passes, typed attractions whose accepted tickets are restricted through interfaces and bounded generics, and atomic pass consumption on entry. Every compatible Delivery 1 and 2 behavior must keep working.

## What Changes

- Reorganize the code into `presentation` (command interaction), `business` (model, rules, controllers) and `persistence` (repositories) packages; introduce a business exception hierarchy handled at the presentation level.
- Make the controllers with unique responsibilities Singletons (`ParkController`, `InvoiceController`); create repositories (behind interfaces) at start-up and inject them, so normal and `--fly` mode differ only in repository configuration. No DI framework.
- Add `HELP EXTENDED` (syntax plus purpose); the help generator receives its rendering strategy by injection.
- **BREAKING (data format)**: passwords are stored only as BCrypt hashes (jBCrypt 0.4 via Maven); `USER DELETE` / `USER UPDATE` verify against the hash; existing plaintext user files are not migrated.
- Add SpeedPasses (`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10, ids `<purchaseIdentifier>:<n>`, 20% of the ticket price) and Special Passes (`SPECIALPASS CREATE;<identifier>;<individualTicket>`, ticket price, one per ticket); refunds with `SPEEDPASS DELETE` / `SPECIALPASS DELETE`; ticket deletions cascade to their passes.
- Add attraction types GENERAL, INDIVIDUAL, GROUP, SPECIAL (`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 3-parameter form creates GENERAL) and `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, with atomic group SpeedPass consumption and automatic Special Pass consumption.
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, generated on demand.
- Persist attraction types and passes (price, ticket, consumed state); update `uml/model.puml`.
- Package the executable JAR with its BCrypt dependency so `java -jar target/nombre-aplicacion.jar` keeps working without extra configuration.

## Capabilities

### New Capabilities
- `layered-packages-and-exceptions`: Presentation/business/persistence package separation and exception-based handling of abnormal situations.
- `invoice-builders`: User and group invoices built through the Builder pattern.
- `singleton-controllers-and-injected-repositories`: Singleton controllers and start-up injection of replaceable repositories.
- `extended-help`: `HELP EXTENDED` and injected help-generation behavior.
- `bcrypt-passwords`: BCrypt protection and verification of user passwords.
- `speed-and-special-passes`: Purchase, pricing, identifiers and refunds of SpeedPasses and Special Passes.
- `generic-attraction-types`: GENERAL / INDIVIDUAL / GROUP / SPECIAL attractions restricted with interfaces and bounded generics.
- `atomic-pass-consumption`: SpeedPass and Special Pass consumption on successful entry only, atomically for groups.
- `pass-persistence-and-uml`: Persistence of passes and attraction types, cascading ticket deletions and the updated UML model.
- `delivery-three-integration`: Delivery 3 technical restrictions and integrated behavior of all three deliveries.

### Modified Capabilities
- `help-and-exit`: `HELP` must also list the Delivery 3 command syntax.
- `attraction-catalog-and-occupancy`: `ATTRACTION CREATE` accepts an optional type (default GENERAL).
- `user-persistence`: the stored password field holds a BCrypt hash instead of the plaintext password.
- `delivery-one-integration`: the "no external runtime libraries / no encryption" restriction is superseded by the Delivery 3 BCrypt dependency.
- `delivery-two-integration`: the "no external libraries" restriction is likewise superseded for BCrypt.

## Impact

- Code: package moves (`command` → `presentation`, domain packages → `business.*`, `park.Park` → `business.controller.ParkController`), new repository interfaces in `business.repository` implemented in `persistence`, new `business.pass`, `business.invoice`, typed attraction classes, help strategies.
- Data files: `users.txt` (password column now a BCrypt hash), `attractions.txt` (new optional type column; old 3-column lines read as GENERAL), new `passes.txt`.
- Build: `org.mindrot:jbcrypt:0.4` runtime dependency; `maven-shade-plugin` bundles it into `target/nombre-aplicacion.jar`.
- Tests: Delivery 1/2 tests move with their packages and keep passing.
- Git: the source prescribes 10 milestone commits; the batch harness controls Git history (see design.md).
