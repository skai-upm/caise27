# Proposal

## Why

Delivery 3 completes the Amusement Park application. Functionally, it adds attraction types that restrict which tickets they accept, personal SpeedPasses and Special Passes bought for individual tickets, on-demand invoices for users and groups, an extended help, and BCrypt-protected passwords. Structurally, it requires the code base to be organised in presentation / business / persistence layers, with singleton controllers, repositories injected as replaceable dependencies, an explicit exception design, the Builder pattern for invoices, and bounded generics for attraction access. The current code has a single `Park` class doing all business work and no explicit layers, so both the behaviour and the structure must change.

## What Changes

- **Layered reorganisation**: packages `presentation` (command processing, help), `business` (model, controllers, exceptions, services) and `persistence` (repository interfaces and text-file implementations). The `Park` god-class is split into singleton controllers (system settings, users, groups, tickets, passes, attractions, invoices). Repositories are plain objects created at start-up (normal or `--fly` paths) and injected into the controllers; controllers are wired to each other during initialisation. No DI framework.
- **Exceptions**: a business exception hierarchy (validation, not found, duplicate, authentication, operation not allowed, persistence) replaces the ad-hoc exceptions; the presentation layer catches them per command.
- **Attraction types** `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`: `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>`; the 3-argument form and stored attractions without type remain `GENERAL`. Ticket access capabilities are interfaces implemented by the ticket types, and attractions are generic over a bounded access type.
- **Passes**: `SPEEDPASS CREATE;<purchaseId>;<ticket>;<1|10>` (passes `<purchaseId>:<n>`, 20% of the ticket price each), `SPECIALPASS CREATE;<id>;<ticket>` (at most one per ticket, price equal to the ticket), `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds for unconsumed passes of future tickets. Passes have their own identifier space, stored price and consumed flag; consumed passes stay recorded.
- **Entry**: `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]` enforces attraction types, consumes one SpeedPass per individual ticket when `SPEED` is used (atomically for groups), and automatically consumes the Special Pass at `SPECIAL` attractions (which reject `SPEED` and group tickets).
- **BREAKING (behaviour)** `TICKET DELETE` and `GROUPTICKET DELETE` also delete all passes of the affected individual tickets.
- **Invoices**: `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupId>`, built with the Builder pattern, including only the sections that apply, never counting member tickets of group tickets twice.
- **Help**: `HELP EXTENDED` shows each command's syntax and purpose; help text generation receives its rendering behaviour by injection.
- **BREAKING (data)** Passwords are stored as BCrypt hashes (jBCrypt 0.4 via Maven, bundled into the executable JAR); verification uses BCrypt. Earlier plain-text `users.txt` files are not migrated.
- Persistence extended with `passes.txt` and the attraction type column; `--fly` covers the new data.
- `HELP` lists every command of the three deliveries; `uml/model.puml` updated to the Delivery 3 design.

Out of scope: the course's mandatory GitLab milestone commit messages (`[F3-C01]` … `[F3-C10]`); repository history is produced by the batch orchestrator, one commit per proposal.

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, identifiers, consumption state and refunds.
- `invoices`: User and group invoices: which purchases each includes, how totals are computed, and section omission.

### Modified Capabilities
- `attractions`: attraction types on creation and type-dependent entry rules including SpeedPass and Special Pass consumption.
- `tickets`: ticket deletions cascade to the passes of the affected individual tickets.
- `command-interface`: supported commands and `HELP` include Delivery 3 commands; new `HELP EXTENDED`.
- `user-management`: passwords are protected with BCrypt and verified against the stored hash.
- `user-persistence`: the users file stores the BCrypt hash instead of the password.
- `park-persistence`: passes and attraction types are persisted.
- `application-runtime`: the executable JAR bundles the BCrypt dependency; `--fly` covers passes.

## Impact

- Code: every class moves to a layer package; new controllers, exception hierarchy, pass and invoice model, attraction subclasses, help renderers, password hasher, repository interfaces. All tests are adapted and extended.
- Build: `org.mindrot:jbcrypt:0.4` dependency; `maven-shade-plugin` produces the self-contained `target/amusement-park.jar`.
- Data: `users.txt` now holds BCrypt hashes (old files with plain passwords will fail authentication); new `passes.txt`; `attractions.txt` gains a fourth column (3-column lines still load as `GENERAL`).
- `uml/model.puml` rewritten for the new design.
