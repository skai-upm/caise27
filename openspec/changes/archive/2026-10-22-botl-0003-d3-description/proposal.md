# Proposal

## Why

Delivery 3 of the Amusement Park assignment completes the park with paid extras (SpeedPasses and Special Passes), attraction types that restrict which tickets they accept, invoices for users and groups, and protected passwords, while reorganizing the code around explicit design mechanisms (layers, exceptions, singleton controllers with dependency injection, Builder, interfaces and bounded generics). Everything from Deliveries 1 and 2 must keep working.

## What Changes

- Reorganize the code into `presentation`, `business` and `persistence` packages; the presentation layer never touches data files and domain objects never persist themselves.
- Represent abnormal situations with a project exception hierarchy handled in the presentation layer; no failure stops command processing.
- Turn the management units into Singleton controllers configured at startup; repositories become replaceable interfaces with file-based implementations created at initialization and injected (no DI framework), so normal and `--fly` modes only differ in configuration.
- Add `SPEEDPASS CREATE`/`DELETE` and `SPECIALPASS CREATE`/`DELETE`: personal passes attached to individual tickets (including group-ticket components), priced from the ticket's stored price (20 % / 100 %), with a consumed flag, refunds only for unconsumed passes of future tickets, and cascade deletion with their tickets.
- Add attraction types `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL` (`ATTRACTION CREATE` gains an optional type; default `GENERAL`) implemented with access-capability interfaces and bounded generics.
- Extend `ATTRACTION ENTER` with an optional `SPEED` flag (consumes one SpeedPass per admitted ticket, atomically) and automatic Special Pass consumption at special attractions.
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<id>` built with the Builder pattern, including only the sections that apply.
- Add `HELP EXTENDED` (syntax plus purpose); help output strategies are injected into the help generator.
- **BREAKING (data)**: passwords are stored as BCrypt hashes (jBCrypt via Maven); `users.txt` files with plain-text passwords from earlier versions are not migrated.
- Persist attraction types and passes (`passes.txt`); update `uml/model.puml`.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, identifiers, consumption state, refunds and cascade deletion.
- `invoices`: On-demand user and group invoices with only the applicable sections and correct totals.
- `password-security`: BCrypt protection of stored passwords and verification of authenticated operations.

### Modified Capabilities
- `attractions`: `Attraction creation` accepts a type; `Entering with a ticket` enforces type restrictions; new requirements for SpeedPass entry and special-attraction entry.
- `tickets`: `Individual ticket deletion` and `Group ticket deletion` also delete the associated passes.
- `command-interface`: `Command syntax` and `HELP command` cover the new commands; new `HELP EXTENDED` requirement.
- `user-persistence`: `Text file storage format` stores the BCrypt hash instead of the password.
- `park-persistence`: `Park data storage` includes attraction types and passes with their price and consumed state.

## Impact

- Code: package reorganization of every class (`presentation`, `business.*`, `persistence.*`); new controllers, exceptions, pass model, typed attractions, invoice builders, help formatters; repository interfaces with file implementations.
- Build: new runtime dependency `org.mindrot:jbcrypt:0.4`; the executable JAR becomes a shaded (self-contained) JAR so `java -jar` still works without extra configuration.
- Data: new `passes.txt`; `attractions.txt` gains an optional type column (old 3-column lines read as `GENERAL`); `users.txt` holds hashes.
- Out of scope: the course's mandatory `[F3-C01]`…`[F3-C10]` student commit roadmap.
