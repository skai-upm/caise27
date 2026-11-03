# Proposal

## Why

Delivery 3 of the Amusement Park assignment (milestones F3-C01…F3-C10, source archived verbatim in `.botl/proposals/…/3.F3_Amusement_Park_Improved_OpenSpec.md`, Appendix A) asks the Delivery 2 application to grow up architecturally and functionally: explicit presentation / business / persistence layers with exceptions as the error mechanism, singleton controllers with injected (replaceable) repositories, Builder-based invoices, a pluggable extended help, BCrypt-protected passwords, SpeedPasses and Special Passes with refunds and cascade deletion, attraction types enforced with interfaces and bounded generics, atomic pass consumption on entry, and persistence and UML covering all of it — while keeping every compatible Delivery 1 and 2 behavior.

## What Changes

- Reorganize the code into `es.upm.etsisi.park.presentation`, `…business` (with `…business.invoice`) and `…persistence`; `App` stays in the root package as composition root. Introduce a `ParkException` hierarchy handled at the presentation level.
- Turn the services into singleton controllers (users, system, groups, tickets, passes, attractions, invoices) configured at start-up with injected collaborators; the park state keeps its single instance and receives a business-defined repository interface implemented by the text-file persistence layer (normal and `--fly` differ only in its path).
- Add `HELP EXTENDED` (syntax plus purpose) with the help text built by a formatter strategy injected into the help generator; `HELP` keeps showing syntax.
- **BREAKING (data, source §8):** store passwords only as BCrypt hashes using the `org.mindrot:jbcrypt:0.4` Maven dependency (bundled into the executable JAR); verify `USER DELETE` / `USER UPDATE` passwords against the hash; old plaintext files are not migrated.
- Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10, ids `<purchaseIdentifier>:<n>`, 20 % of the ticket price each, atomic), `SPECIALPASS CREATE;<identifier>;<individualTicket>` (ticket price, at most one per ticket), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds (unused, ticket strictly in the future); consumed passes stay as records; valid ticket and group ticket deletions cascade to their passes atomically.
- Add attraction types GENERAL / INDIVIDUAL / GROUP / SPECIAL via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` (the 3-parameter form creates GENERAL), enforced through ticket access interfaces and bounded generic attraction classes.
- Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consuming one unused SpeedPass per entering ticket (all-or-nothing for groups), and automatic Special Pass consumption at SPECIAL attractions (which reject `SPEED` and group tickets).
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>` built with the Builder pattern, showing only applicable sections and never double-counting group-composed tickets or members' passes.
- Persist attraction types, passes (ticket, price, consumed state); update `uml/model.puml`; extend `HELP` with every Delivery 3 command.

## Capabilities

### New Capabilities
- `layered-packages-and-exceptions`: Presentation / business / persistence packages and exception-based error propagation handled without stopping command processing.
- `invoice-builders`: User and group invoices built on demand with a Builder, applicable sections only, correct item prices and totals.
- `singleton-controllers-and-injected-repositories`: Singleton controllers, repositories created at start-up and injected, controller collaborations injected, no DI framework.
- `extended-help`: `HELP EXTENDED` with purposes, help formatting behavior injected into the help generator.
- `bcrypt-passwords`: BCrypt-hashed password storage and verification via a Maven dependency.
- `speed-and-special-passes`: Purchase, pricing, consumed state and refunds of SpeedPasses and Special Passes.
- `generic-attraction-types`: GENERAL / INDIVIDUAL / GROUP / SPECIAL attractions restricted by access interfaces and bounded generics.
- `atomic-pass-consumption`: SpeedPass and Special Pass consumption only on successful, eligible, atomic entry.
- `pass-persistence-and-uml`: Persistence of passes and attraction types, cascade deletion of passes, updated UML model.
- `delivery-three-integration`: Delivery 3 technical restrictions and coexistence of all commands of Deliveries 1–3 in every execution mode.

### Modified Capabilities
- `help-and-exit`: `HELP` must also list the Delivery 3 commands, including `HELP EXTENDED`.
- `user-persistence`: the stored password field holds the BCrypt hash instead of the plaintext password.
- `delivery-one-integration`: the Delivery 1 dependency/encryption prohibition gains the Delivery 3 BCrypt exception.
- `delivery-two-integration`: relationship rules extended to passes; dependency exception for BCrypt.
- `attraction-catalog-and-occupancy`: `ATTRACTION CREATE` accepts an optional type, defaulting to GENERAL.
- `individual-attraction-entry`: entry is also rejected when the attraction type does not accept the ticket or required passes are missing.
- `atomic-group-entry-and-exit`: group entry is limited to attraction types that accept group tickets.

## Impact

- Code: every main class moves to a layer package; services become controllers; new pass, attraction-type, invoice, help and exception classes; `CommandProcessor` supports alternative parameter counts.
- Build: new runtime dependency `org.mindrot:jbcrypt:0.4`; `maven-shade-plugin` packages it into `target/amusement-park.jar` so `java -jar` keeps working without extra configuration.
- Data: `users.txt` stores hashes; `attractions.txt` gains a type field (3-field lines read as GENERAL); new `passes.txt`.
- Docs: `uml/model.puml` rewritten for the final design.
- Git: ten prescribed milestone commit messages; handling under the batch harness recorded in design.md.
