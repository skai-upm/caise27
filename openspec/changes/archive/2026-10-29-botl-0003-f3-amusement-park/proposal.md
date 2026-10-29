# Proposal

## Why

Delivery 3 of the Amusement Park assignment restructures the Delivery 2 application into a layered design (exceptions, singleton controllers, hand-written dependency injection) and adds several features:
- BCrypt-protected passwords;
- SpeedPasses and Special Passes;
- typed attractions restricted through interfaces and bounded generics;
- pass consumption on entry;
- Builder-based invoices;
- `HELP EXTENDED`.

This change implements milestones F3-C01 through F3-C10 together, on top of the Delivery 2 code.

## What Changes

- **F3-C01**: Move code into `presentation`, `business` (model, controllers, invoices, validation, security, exceptions) and `persistence` packages. Introduce a project exception hierarchy used to signal and propagate abnormal situations, handled at the presentation level so command processing never terminates unexpectedly.
- **F3-C02**: Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, built with an explicit Builder (common builder abstraction, user and group concrete builders, director) that includes only non-empty, relevant sections and never double-counts composition tickets.
- **F3-C03**: Make the controllers with unique responsibilities singletons: park, user, pass and invoice controllers. Repositories become interfaces with text-file implementations created in an application initializer and injected into the controllers, along with controller-to-controller collaborations. The only difference in `--fly` mode is the configured paths.
- **F3-C04**: Add `HELP EXTENDED`. The help generator receives its formatting behaviour (syntax-only or syntax plus description) through injection.
- **F3-C05**: Add the `org.mindrot:jbcrypt:0.4` dependency, bundled into the executable JAR. Passwords are validated with the Delivery 1 rules, stored only as BCrypt hashes, and verified with BCrypt for `USER DELETE` and `USER UPDATE`; a password change stores a new hash. **BREAKING**: `users.txt` files from earlier deliveries hold plain-text passwords, which no longer authenticate (migration is not required by the source).
- **F3-C06**: Add `SPEEDPASS CREATE;<purchaseId>;<individualTicket>;<1|10>` (passes `<purchaseId>:<n>` at 20 % of the ticket price), `SPECIALPASS CREATE;<id>;<individualTicket>` (at most one per ticket, at the ticket price), and `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds (unconsumed, future ticket only). Deleting a ticket or group ticket cascades to its passes.
- **F3-C07**: Add attraction types `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL` via `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>`; the 4-field form still creates `GENERAL`. Access capabilities are interfaces implemented by tickets, and attractions are bounded generic types.
- **F3-C08**: Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, which consumes exactly one SpeedPass per entering ticket, atomically for groups. SPECIAL attractions consume the ticket's Special Pass automatically and reject `SPEED` and group tickets.
- **F3-C09**: Persist attraction types and passes (price, ticket, consumed state) and update `uml/model.puml`.
- **F3-C10**: Extend `HELP` to all three deliveries, and integrate and verify.

## Capabilities

### New Capabilities
- `application-architecture`: Layered packages, exception-based error handling, singleton controllers and dependency injection.
- `passes`: SpeedPass and Special Pass purchase, pricing, consumption state and refunds.
- `invoices`: User and group invoice generation with the Builder pattern.

### Modified Capabilities
- `attractions`: Attraction types and type-based entry restrictions; SpeedPass and Special Pass use on entry.
- `tickets`: Ticket and group ticket deletion cascade to associated passes; tickets expose access capabilities.
- `user-persistence`: The users file stores BCrypt hashes instead of plain passwords.
- `user-validation`: Passwords are protected and verified with BCrypt.
- `park-persistence`: Attraction type and passes are persisted.
- `command-interface`: HELP covers Delivery 3 commands; `HELP EXTENDED` is added.
- `build-packaging`: The BCrypt dependency is allowed and bundled in the executable JAR.
- `design-documentation`: The UML model reflects the Delivery 3 design.

## Impact

- All classes move from `es.upm.amusementpark` into `presentation`, `business.*` and `persistence.*` subpackages. `App` stays in the root package as the composition root.
- `pom.xml` gains `jbcrypt` and the `maven-shade-plugin`, so `target/amusement-park.jar` remains self-contained.
- New data file `passes.txt`. `attractions.txt` gains a type column, and old 3-field lines are read as GENERAL.
- The tests are reorganised per package.
- Git: the ten milestone commit messages are recorded in the single BOTL commit body (flagged, as for earlier deliveries).
