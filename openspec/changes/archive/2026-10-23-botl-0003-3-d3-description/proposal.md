# Proposal

## Why

Delivery 3 (source: `.botl/proposals/opus_4_description_nl/0003-3.D3.-description.md/3.D3.-description.md`) completes the Amusement Park application. It adds paid extras to individual tickets, typed attractions that restrict which tickets and passes they accept, invoices for users and groups, and protection of stored passwords. It also asks for a formal object-oriented design:
- layered packages
- exceptions for abnormal situations
- Singleton controllers
- dependency injection of replaceable repositories
- the Builder pattern for invoices
- bounded generics over access-capability interfaces for attractions

Everything from Deliveries 1 and 2 must keep working unless this delivery explicitly changes it.

## What Changes

- **Layered organization.** Code moves into `presentation`, `business`, and `persistence` packages under `es.upm.amusementpark`, with a composition root that wires the application.
- **Exceptions.** Abnormal situations use an exception hierarchy: invalid data, not found, operation not allowed, and persistence failure. These are caught per command, so processing never stops unexpectedly.
- **Controllers and dependency injection.**
  - One Singleton controller per management responsibility: system settings, users, groups, tickets, passes, attractions, and invoices, coordinated by the central `Park`.
  - Repositories are interfaces with file implementations. They are not singletons: they are created at startup, configured with the data directory (normal or `--fly`), and injected.
  - Collaborations between controllers are also injected at startup.
- **Invoices built with Builder.** `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupId>` create invoices through a common builder abstraction with user and group builders. Only the sections that have data are included, and items already priced inside a group ticket are never counted twice.
- **`HELP EXTENDED`.** It shows each command's syntax with a description. Help text generation receives its formatting behavior through dependency injection.
- **BREAKING (data): BCrypt passwords.** Passwords are stored only as BCrypt hashes (jBCrypt 0.4 via Maven) and verified with BCrypt. User files written by earlier versions with plain-text passwords are not migrated: their lines are skipped with a warning.
- **Passes.**
  - `SPEEDPASS CREATE` (quantity 1 or 10, ids `<purchase>:<n>`, 20% of the ticket's stored price each) and `SPEEDPASS DELETE`.
  - `SPECIALPASS CREATE` (at most one per ticket ever, same price as the ticket) and `SPECIALPASS DELETE`.
  - Passes have a consumed flag. Only unconsumed passes on tickets dated after today can be refunded.
  - Deleting a ticket or group ticket cascades to its passes.
- **Attraction types.** `ATTRACTION CREATE` takes an optional `<type>`: GENERAL (the default), INDIVIDUAL, GROUP, or SPECIAL. Access-capability interfaces implemented by tickets, together with bounded generic attraction classes, restrict the accepted tickets.
- **Entry changes.**
  - `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` consumes one SpeedPass per entering ticket, atomically for groups.
  - SPECIAL attractions automatically consume the ticket's Special Pass and refuse SPEED.
  - `ATTRACTION STATUS` also shows the type.
- **Persistence.** Pass data (type, owner ticket, stored price, consumed flag) and attraction types are persisted. Old attraction lines without a type load as GENERAL. `--fly` covers the new files.
- **HELP** lists all commands from the three deliveries.
- The **executable JAR** bundles the BCrypt library, so `java -jar` still works without extra configuration.
- **UML** is updated to the Delivery 3 design.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, identifiers, consumption state, refunds, and cascading deletion with their tickets.
- `invoices`: On-demand user and group invoices, with the sections and totals each one must contain.
- `architecture`: Structural constraints the delivery mandates: layered packages, exception handling, Singleton controllers, injected replaceable repositories, the Builder for invoices, injected help formatting, and bounded generics for attraction access.

### Modified Capabilities
- `application-build`: The runtime may use the BCrypt library, which is packaged into the executable JAR.
- `command-interface`: New keywords, HELP output for all deliveries, and the new `HELP EXTENDED` command.
- `user-management`: Passwords are protected with BCrypt and verified against the stored hash.
- `user-persistence`: The user file stores BCrypt hashes, and lines with plain-text passwords are skipped.
- `attractions`: Attraction types, type-dependent entry rules, SpeedPass and Special Pass use on entry, and the type in the status output.
- `tickets`: Deleting an individual ticket also deletes its passes.
- `group-tickets`: Deleting a group ticket also deletes the passes of its individual tickets.
- `park-persistence`: Passes and attraction types are persisted and reloaded.
- `design-model`: The UML diagram covers the Delivery 3 design elements.

## Impact

- Code: package moves for every class, new controllers, exceptions, invoices, passes, attraction subclasses, access interfaces, and help formatters, plus a rewritten `App` composition root. Tests are updated to the new packages and controller APIs.
- Data files: `users.txt` holds hashes; `attractions.txt` gains a fourth field; a new `passes.txt` file.
- Dependencies: `org.mindrot:jbcrypt:0.4` (runtime), shaded into the executable JAR with `maven-shade-plugin`.
- Out of scope: the mandatory `[F3-Cxx]` Git milestone commits. BOTL produces one commit per proposal; task groups follow the milestones.
