# Proposal

## Why

Delivery 3 (milestones F3-C01 to F3-C10) evolves the Delivery 2 park into an explicitly layered application with governed dependencies, and adds new economic and access features: invoices, SpeedPasses and Special Passes, typed attractions and transactional pass consumption, while protecting stored passwords. The source requires this as one consolidated change that keeps Delivery 1 and 2 behaviour working.

## What Changes

- Reorganise the code into non-default `presentation`, `business` and `persistence` packages; represent abnormal situations with a business exception hierarchy handled at the presentation level so that no command aborts processing (F3-C01).
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, built on demand through the Builder pattern with only applicable sections, item prices and totals, without double counting group-ticket constituents or members' personal passes (F3-C02).
- Apply Singleton to the controllers with unique responsibility; create repositories at start-up and inject them (and controller collaborations) by hand-written dependency injection, so normal and `--fly` modes differ only in repository configuration (F3-C03).
- Add `HELP EXTENDED` (syntax plus purpose); help text is produced by a generator that receives its formatting strategy by injection (F3-C04).
- **BREAKING (storage format):** store user passwords only as BCrypt hashes using the `org.mindrot:jbcrypt:0.4` Maven dependency, after the Delivery 1 validation; `USER DELETE` / `USER UPDATE` verify against the hash; old plaintext files are not migrated (F3-C05).
- Add SpeedPasses (`SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` with quantity 1 or 10, ids `<purchaseIdentifier>:<n>`, 20% of the ticket's stored price each) and Special Passes (`SPECIALPASS CREATE;<identifier>;<individualTicket>`, price equal to the ticket's stored price, at most one per ticket), with consumed state and refunds via `SPEEDPASS DELETE` / `SPECIALPASS DELETE` (F3-C06).
- Add attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL (`ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`; the 3-parameter form means GENERAL), enforced with ticket access interfaces and bounded generics (F3-C07).
- Extend `ATTRACTION ENTER` with optional `;SPEED` (one unused SpeedPass consumed per entrant, all-or-nothing for groups) and automatic Special Pass consumption at SPECIAL attractions (F3-C08).
- Persist passes (ticket association, stored price, consumed state) and attraction types in normal and `--fly` modes; valid ticket and group-ticket deletions cascade to their passes atomically; update `uml/model.puml` (F3-C09).
- Extend `HELP` / `HELP EXTENDED` with all Delivery 1–3 commands and integrate end to end (F3-C10).

## Capabilities

### New Capabilities

- `layered-packages-and-exceptions`: Layered package organisation and controlled exception handling (F3-C01).
- `invoice-builders`: User and group invoices built with the Builder pattern (F3-C02).
- `singleton-controllers-and-injected-repositories`: Singleton controllers and injected, replaceable repositories (F3-C03).
- `extended-help`: `HELP EXTENDED` and strategy-based help generation (F3-C04).
- `bcrypt-passwords`: BCrypt-protected stored passwords (F3-C05).
- `speed-and-special-passes`: Pass purchase, pricing, consumed state and refunds (F3-C06).
- `generic-attraction-types`: Typed attractions restricted by access interfaces and bounded generics (F3-C07).
- `atomic-pass-consumption`: SpeedPass and Special Pass consumption on entry (F3-C08).
- `pass-persistence-and-uml`: Persistence of passes and attraction types, cascade deletes, updated UML (F3-C09).
- `delivery-three-integration`: Delivery 3 technical restrictions and end-to-end behaviour (F3-C10).

### Modified Capabilities

- `user-persistence`: the users file stores a BCrypt hash instead of the password.
- `delivery-one-integration`: the "no external libraries / no encryption" restriction is relaxed for the BCrypt dependency from Delivery 3.
- `delivery-two-integration`: same relaxation of the standard-library-only restriction.
- `help-and-exit`: `HELP` lists the Delivery 3 commands.
- `attraction-catalog-and-occupancy`: attraction creation accepts an optional type.
- `individual-attraction-entry`: individual entry is subject to the attraction type.
- `atomic-group-entry-and-exit`: group entry is subject to the attraction type.
- `individual-ticket-management`: valid `TICKET DELETE` also deletes the ticket's passes.
- `group-ticket-purchasing`: valid `GROUPTICKET DELETE` also deletes the constituents' passes.

## Impact

- Package moves for every class (`presentation`, `business.*`, `persistence`); `App` and the composition root stay in the root package. Main class unchanged.
- New Maven runtime dependency `org.mindrot:jbcrypt:0.4`; the executable JAR must therefore bundle it (shaded JAR).
- New data file `passes.txt`; `attractions.txt` gains a type column (old 3-column lines read as GENERAL); `users.txt` stores BCrypt hashes.
- `uml/model.puml` updated.
- Git: the source prescribes 10 separate milestone commits; see design.md.
