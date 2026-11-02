# Proposal

## Why

Delivery 2 lives in a single package, with one `Park` class owning every rule, plain-text passwords, and one attraction kind. Delivery 3 needs several things:

- An explicit layered architecture: presentation, business, persistence.
- Singleton controllers with injected, replaceable repositories.
- BCrypt-protected passwords.
- Builder-generated invoices.
- SpeedPasses and Special Passes.
- Attraction types enforced with interfaces and bounded generics.
- Atomic pass consumption on entry.

All compatible Delivery 1 and 2 behavior must be kept.

## What Changes

- Reorganize code into the packages `es.upm.etsisi.park.presentation`, `…business` (with `…business.invoice`) and `…persistence`. The entry point stays in the root package `es.upm.etsisi.park`. Exceptions propagate abnormal situations and are handled at the command level. (F3-C01)
- Add `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`. Invoices are built on demand through a Builder abstraction with a user builder and a group builder, include only applicable sections, and never double-count group constituents or personal passes. (F3-C02)
- Split the Delivery 2 `Park` into singleton controllers (users, groups, tickets, passes, attractions, invoices) around the singleton `Park` state holder. The controllers are wired in a hand-written composition root. The repository is a business-layer interface, implemented by text files in the persistence layer, created at start-up and injected. Normal and `--fly` modes differ only in the repository directory. (F3-C03)
- Keep `HELP` (syntax) and add `HELP EXTENDED` (syntax plus purpose). Both are generated from one command registry by a help generator that receives its formatting strategy through injection. (F3-C04)
- **BREAKING (data):** store passwords only as BCrypt hashes, using the Maven dependency `org.mindrot:jbcrypt:0.4`, which is bundled into the executable JAR. Delivery 1 validation still runs on the plain password first, and DELETE/UPDATE verify against the hash. Migrating old plaintext files is not required. (F3-C05)
- Add `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` (quantity 1 or 10, ids `<purchaseIdentifier>:<n>`, 20% of the ticket price each, all-or-nothing) and `SPECIALPASS CREATE;<identifier>;<individualTicket>` (ticket price, at most one per ticket). Add refunds with `SPEEDPASS DELETE` and `SPECIALPASS DELETE`, allowed only when the pass is unused and the ticket date is strictly future. Ticket and group-ticket deletion cascade to their passes. (F3-C06)
- Add the attraction types GENERAL, INDIVIDUAL, GROUP and SPECIAL, created via `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`. The 4-part form creates GENERAL. Access is restricted by ticket capability interfaces and `Attraction<T extends AccessTicket>`. (F3-C07)
- Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, which consumes one unused SpeedPass per entering ticket, atomically for groups. SPECIAL attractions consume the ticket's Special Pass automatically and reject SPEED. (F3-C08)
- Persist attraction type and passes (ticket, stored price, consumed flag) in normal and `--fly` modes, and update `uml/model.puml`. (F3-C09)
- **Modify** `HELP` to include all Delivery 3 commands. **Modify** the Delivery 1 and 2 technical-restriction requirements to allow the BCrypt dependency. **Modify** the users-file format requirement so the password column holds the BCrypt hash. (F3-C10)

## Capabilities

### New Capabilities

- `layered-packages-and-exceptions`: Presentation, business and persistence packages, plus controlled exception handling.
- `invoice-builders`: User and group invoices built with the Builder pattern.
- `singleton-controllers-and-injected-repositories`: Singleton controllers, injected replaceable repositories, no DI framework.
- `extended-help`: `HELP EXTENDED` and injected help-generation strategy.
- `bcrypt-passwords`: BCrypt hashing and verification of user passwords.
- `speed-and-special-passes`: Pass purchase, pricing, consumed state, refunds and cascade deletion.
- `generic-attraction-types`: The four attraction types, enforced with access interfaces and bounded generics.
- `atomic-pass-consumption`: SPEED entry and automatic Special Pass consumption, atomic with entry.
- `pass-persistence-and-uml`: Persistence of passes and attraction types, and the Delivery 3 UML model.
- `delivery-three-integration`: Delivery 3 technical restrictions and coexistence of all three deliveries.

### Modified Capabilities

- `help-and-exit`: HELP lists Delivery 1–3 command syntax, including `HELP EXTENDED`.
- `user-persistence`: The stored password column holds a BCrypt hash rather than the password.
- `delivery-one-integration`: The external-library and encryption prohibition no longer applies to the BCrypt dependency from Delivery 3 onward.
- `delivery-two-integration`: Same BCrypt exception to "no external libraries".

## Impact

- Every source file moves package. `App` stays the JAR entry point, so the manifest `mainClass` stays `es.upm.etsisi.park.App`.
- `pom.xml` gains `org.mindrot:jbcrypt:0.4` (compile scope) and `maven-shade-plugin`, so `target/nombre-aplicacion.jar` remains runnable with `java -jar`.
- Data: a new `passes.txt`; `attractions.txt` gains a type column (old 3-field lines still load as GENERAL); `users.txt` now stores hashes.
- `uml/model.puml` is rewritten.
- Git: the source prescribes 10 milestone commits, but the BOTL harness owns history. See design.md.
