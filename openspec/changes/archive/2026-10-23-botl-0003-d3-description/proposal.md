# Proposal

## Why

Delivery 3 of the Amusement Park assignment completes the park with personal passes, typed attractions and invoices, and formalises the design: layered packages, exception-based error handling, singleton controllers with injected (replaceable) repositories, the Builder pattern for invoices, injected help formatting, and BCrypt-protected passwords. Deliveries 1 and 2 behaviour must keep working except where this delivery explicitly changes it.

## What Changes

- Reorganise the code into `presentation`, `business` and `persistence` packages; the presentation layer never touches files and domain objects never persist themselves.
- Represent abnormal situations with a project exception hierarchy handled at the command-processing level.
- Make unique controllers singletons; create repositories (behind interfaces) at start-up and inject them, and inject controller collaborations, so `--fly` only changes repository configuration.
- Add SpeedPasses (`SPEEDPASS CREATE;<purchaseId>;<ticket>;<1|10>`, ids `<purchaseId>:<n>`, 20% of the ticket price) and Special Passes (`SPECIALPASS CREATE;<id>;<ticket>`, at most one per ticket, price = ticket price), with refunds (`SPEEDPASS DELETE`, `SPECIALPASS DELETE`) and cascading deletion with their ticket.
- Add attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` (`ATTRACTION CREATE;...;<capacity>;<type>`, 4-field form still creates GENERAL) restricted through access interfaces and bounded generics.
- **Modify** `ATTRACTION ENTER`: enforce type restrictions, add `;SPEED` (consumes one SpeedPass per entering ticket, atomic for groups) and automatic Special Pass consumption at SPECIAL attractions.
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupId>` built with the Builder pattern from the persisted state.
- Add `HELP EXTENDED` (syntax plus purpose of each command) with help formatting injected into the help generator; **modify** `HELP` to list all commands.
- **BREAKING (data)**: passwords are stored only as BCrypt hashes (jBCrypt via Maven); plain-text user files from earlier versions are not migrated.
- Extend persistence with attraction types (missing type = GENERAL) and passes with price and consumed state.
- Update `uml/model.puml`.

## Capabilities

### New Capabilities
- `ticket-passes`: SpeedPass and Special Pass purchase, pricing, identifiers, consumption state, refunds and cascading deletion.
- `invoices`: User and group invoices with only the applicable sections and correct totals.

### Modified Capabilities
- `attractions`: Creation with a type; entry restricted by attraction type, optional SpeedPass use and automatic Special Pass consumption.
- `command-interface`: `HELP` lists all three deliveries' commands; new `HELP EXTENDED`.
- `park-tickets`: Ticket and group ticket deletion cascade to associated passes.
- `park-persistence`: Persist attraction types and passes; `--fly` covers passes.
- `user-persistence`: The users file stores a BCrypt hash instead of the password.
- `user-management`: Password verification for authenticated operations uses the stored BCrypt hash.

## Impact

- Package moves for every class (`es.upm.park.presentation`, `es.upm.park.business.*`, `es.upm.park.persistence[.file]`); tests move accordingly.
- `pom.xml`: `org.mindrot:jbcrypt:0.4` runtime dependency; executable JAR becomes self-contained (dependencies bundled) so `java -jar` keeps working.
- New data file `passes.txt`; `attractions.txt` gains a type column; `users.txt` stores hashes.
- README and `uml/model.puml` updated.
