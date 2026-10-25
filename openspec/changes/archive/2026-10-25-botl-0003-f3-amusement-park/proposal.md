# Proposal

## Why

Delivery 3 makes the code base structurally sound and adds new paid features. All classes currently sit in one package and one large `Park` controller. Passwords are stored in plain text. There are no invoices, no speed or special passes, and only one kind of attraction. The delivery requires layered packages, explicit design patterns (Builder, Singleton, dependency injection), hashed passwords through a Maven dependency, and new pass and attraction features, while every E1/E2 command keeps working.

## What Changes

- **Layering (F3-C01):** code moves to `com.amusementpark.presentation` (start-up, command processing, help), `com.amusementpark.business` (`model`, `controller`, `invoice`, `validation`, `security`) and `com.amusementpark.persistence` (repositories and file access). Command syntax and behaviour are unchanged.
- **Invoices with Builder (F3-C02):** `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupId>` build an on-demand (not persisted) invoice step by step. User invoices cover the user's directly bought tickets and the passes on any of their tickets. Group invoices cover the group's group tickets only.
- **Singleton controllers + dependency injection (F3-C03):** the single `Park` controller is split into single-responsibility controllers (park settings, users, groups, tickets, attractions, invoices), each a Singleton. Repositories are not singletons. They are created at start-up with the normal or `--fly` paths and injected, together with controller-to-controller collaborators.
- **Extended help (F3-C04):** `HELP` keeps the syntax list. `HELP EXTENDED` adds a description per command. Both come from a registry of commands, and the output format is an injected strategy.
- **Hashed passwords (F3-C05):** a BCrypt Maven dependency is added. Passwords are validated with the E1 rules, stored as BCrypt hashes, and verified on `USER DELETE`/`USER UPDATE`. Legacy plain-text entries are migrated on load. The executable JAR bundles the dependency. **BREAKING**: the users file stores hashes instead of passwords.
- **Personal passes (F3-C06):** `SPEEDPASS CREATE;<ticketId>;<1|10>` and `SPECIALPASS CREATE;<ticketId>` buy passes for an individual ticket. A SpeedPass costs 20% of the ticket price; there is at most one Special Pass at the ticket price. `SPEEDPASS DELETE;<passId>` and `SPECIALPASS DELETE;<passId>` refund unused passes of future tickets.
- **Typed attractions with generics (F3-C07):** GENERAL, INDIVIDUAL, GROUP and SPECIAL attraction types use an access interface and bounded generic attraction classes. `ATTRACTION CREATE;Id;Name;Capacity[;Type]` defaults to GENERAL.
- **Pass consumption on entry (F3-C08):** `ATTRACTION ENTER;<id>;<ticket>[;SPEED]` consumes SpeedPasses atomically (one per admitted user). SPECIAL attractions consume the ticket's Special Pass and reject SPEED. Consumption is reported.
- **Persistence + UML (F3-C09):** passes (price, ticket, consumed state) and the attraction type persist, including under `--fly`. Deleting a ticket or group ticket cascades to its passes without partial deletion. `uml/model.puml` is updated.
- **Integration (F3-C10):** all E1–E3 commands, interactive/file/`--fly` modes and the executable JAR work together.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering, Singleton controllers and start-up dependency injection.
- `invoicing`: On-demand user and group invoices built with a Builder.
- `personal-passes`: SpeedPass and Special Pass purchase, refund, consumption state and persistence.

### Modified Capabilities
- `command-interface`: `HELP` lists the new commands; `HELP EXTENDED` is added.
- `user-management`: passwords are stored as hashes and verified against them.
- `user-persistence`: the users file stores the password hash.
- `project-build`: a runtime BCrypt dependency is allowed and bundled in the executable JAR.
- `attractions`: attraction types, optional type on creation, SPEED and Special Pass entry rules, type persistence.
- `ticketing`: ticket and group-ticket deletion cascade to passes.

## Impact

- All Java sources move packages; the manifest main class becomes `com.amusementpark.presentation.App`.
- `pom.xml`: `org.mindrot:jbcrypt` dependency and `maven-shade-plugin` for an executable JAR with dependencies.
- New data file `passes.txt`; `attractions.txt` gains a type column (old 3-field lines read as GENERAL); `users.txt` stores hashes.
- `uml/model.puml` rewritten for the E3 design.
- Commits: the delivery lists ten mandatory commit messages (`[F3-C01]`…`[F3-C10]`). The batch tooling running this change produces one commit per proposal, so the ten labels are recorded in that commit's message instead of as separate commits.
