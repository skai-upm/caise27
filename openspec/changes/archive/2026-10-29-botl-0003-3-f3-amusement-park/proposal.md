# Proposal

## Why

Amusement Park Delivery 3 (milestones F3-C01 to F3-C10) asks for a cleaner architecture (layers, exceptions, Singleton controllers with dependency injection), stronger security (BCrypt passwords) and new economic and access features (SpeedPasses, Special Passes, typed attractions with generics, invoices built with the Builder pattern), while keeping every Delivery 1 and 2 behaviour working.

## What Changes

- **F3-C01** Reorganise the code into `presentation`, `business` and `persistence` packages under `es.upm.etsisi.amusementpark`; introduce an exception hierarchy used to propagate abnormal situations, handled in the presentation layer.
- **F3-C02** Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, built with the Builder pattern (common builder abstraction, user and group builders, only non-empty sections).
- **F3-C03** Make the management controllers Singletons (users, park, passes, invoices); repositories become interfaces with text-file implementations, created in a bootstrap class and injected (normal vs `--fly` paths). No DI framework.
- **F3-C04** Add `HELP EXTENDED` (syntax + purpose); help generation receives its formatting behaviour by injection.
- **F3-C05** Store passwords as BCrypt hashes (`org.mindrot:jbcrypt:0.4`), verify them for USER DELETE / USER UPDATE, re-hash on change. The executable JAR bundles the dependency. **BREAKING:** plain-text passwords in existing `users.txt` no longer authenticate (migration not required by the source).
- **F3-C06** Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (1 or 10, ids `<purchase>:<n>`), `SPECIALPASS CREATE;<identifier>;<individualTicket>` (at most one per ticket), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds, pricing (20 % / 100 % of the ticket's stored price), consumed state, and cascade deletion with TICKET DELETE / GROUPTICKET DELETE.
- **F3-C07** Add attraction types GENERAL, INDIVIDUAL, GROUP, SPECIAL (`ATTRACTION CREATE;...;<type>`, 4-field form = GENERAL) using access interfaces implemented by tickets and bounded generic attraction classes.
- **F3-C08** Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` and automatic Special Pass consumption; group entry with SpeedPasses is atomic.
- **F3-C09** Persist attraction types and passes (price, ticket, consumed state); update `uml/model.puml`.
- **F3-C10** Extend HELP with all new syntax and verify everything works together.

## Capabilities

### New Capabilities
- `application-architecture`: Layered packages, exception-based error propagation, Singleton controllers and dependency injection.
- `invoicing`: User and group invoices built with the Builder pattern.
- `personal-passes`: SpeedPass and Special Pass purchase, pricing, refunds, consumption state and cascades.
- `password-security`: BCrypt protection and verification of user passwords.

### Modified Capabilities
- `application-build`: The standard-library-only rule now allows the BCrypt dependency.
- `command-processing`: Recognised commands and HELP extended; HELP EXTENDED added.
- `user-persistence`: Stored password field holds the BCrypt hash.
- `attractions`: Attraction creation accepts a type; entry rules depend on type and passes.
- `ticketing`: Ticket deletions cascade to associated passes.
- `park-persistence`: Attraction types and passes are persisted.
- `design-model`: The diagram reflects the Delivery 3 design.

## Impact

- All Java sources move into layered packages; `App` stays as the bootstrap entry point (`es.upm.etsisi.amusementpark.App`).
- `pom.xml`: jbcrypt runtime dependency and maven-shade-plugin so `target/amusement-park.jar` remains self-contained.
- New runtime file `passes.txt` (git-ignored); `attractions.txt` gains a type column (old 3-field lines read as GENERAL).

## Ambiguities flagged (not silently resolved)

1. **Milestone commits**: ten exact-message commits are requested; the BOTL batch produces one commit for this proposal and lists them in its body.
2. **Special Pass after refund**: "at most one Special Pass throughout its entire existence" vs. "deleting a pass … ceases to be part of the persisted state". Interpreted as: a ticket may not hold a second Special Pass while one exists (consumed or not); after a refund a new one may be bought.
3. **Pass identifier space** is shared by SpeedPasses and Special Passes (both are "passes"); it is independent from tickets.
4. **Passes on historical tickets / invoice scope**: invoices include all (also historical) tickets and passes still stored, since they are fiscal records.
5. **Composition ticket at SPECIAL / INDIVIDUAL attractions**: a composition ticket is an individual ticket, so it may enter these attractions (Special Pass required at SPECIAL).
6. **Group entry with SPEED** requires an unconsumed SpeedPass on every composition ticket; one per ticket is consumed. Which pass is consumed when several exist: the earliest purchased (output names it).
7. **Exception taxonomy** is a design choice: validation, not-found, conflict, forbidden-operation and authentication exceptions under one checked base, plus an unchecked persistence exception.
8. **Legacy plaintext passwords** cannot be verified after the upgrade (no migration required); such users must be recreated.
