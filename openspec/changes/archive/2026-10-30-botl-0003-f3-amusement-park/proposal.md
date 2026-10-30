# Proposal

## Why

Deliveries 1–2 built a working park application in a single package with service classes created by one controller. Delivery 3 (F3) must harden it: layered packages, unique controllers with injected dependencies, hashed passwords, invoices built with the Builder pattern, personal passes (SpeedPass / Special Pass), attraction types that restrict tickets through interfaces and bounded generics, pass-aware atomic entry, and persistence/UML for the new state — all as one change that keeps every E1/E2 command working. The source mandates ten milestone commits (F3-C01…F3-C10) with exact messages.

## What Changes

- **F3-C01 Layers**: move code to `es.upm.amusementpark.presentation` (App, command processing, help), `business` (model, controllers, validation, security, invoices) and `persistence` (repositories, storage location). No behavior change.
- **F3-C02 Invoices**: `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` built step by step by an `InvoiceBuilder`; user invoices list directly bought individual tickets (not group constituents) and, from F3-C06, the user's personal passes; group invoices list group tickets. Not persisted.
- **F3-C03 Controllers**: unique controllers (`ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, later `PassController`, `InvoiceController`) become Singletons configured at startup; repositories are ordinary objects created at startup (paths from normal or `--fly` storage) and injected, as are inter-controller dependencies.
- **F3-C04 Help**: `HELP` stays concise; `HELP EXTENDED` adds a description per command. Both come from one command registry; the help generator receives its formatters by injection.
- **F3-C05 Passwords**: Maven dependency `org.mindrot:jbcrypt`; E1 format validation, then BCrypt hashing on create and password change; `USER DELETE`/`USER UPDATE` verify against the hash.
- **F3-C06 Passes**: `SPEEDPASS CREATE;<idTicket>;<1|10>` (each 20 % of the ticket's stored price), `SPECIALPASS CREATE;<idTicket>` (one per ticket, price = ticket price), `SPEEDPASS DELETE;<idPase>` / `SPECIALPASS DELETE;<idPase>` refund unused passes of tickets dated after the system date. Invoices gain the pass section.
- **F3-C07 Attraction types**: `GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`; `ATTRACTION CREATE;<id>;<nombre>;<aforo>[;<TIPO>]` (default `GENERAL`); ticket access interfaces and `Attraction<T extends AccessTicket>` subclasses restrict accepted tickets.
- **F3-C08 Pass entry**: `ATTRACTION ENTER;<id>;<ticket>[;SPEED]`; atomic consumption of one SpeedPass per admitted user; SPECIAL attractions consume the ticket's Special Pass automatically and reject `SPEED`.
- **F3-C09 Persistence/UML**: passes persisted (`passes.txt`) with price, ticket and consumption status; attraction type persisted; cascade deletion of passes with tickets/group tickets without partial state; UML updated.
- **F3-C10 Integration**: HELP/HELP EXTENDED list all commands; interactive, file and `--fly` modes verified end to end with the packaged JAR.
- **BREAKING (persistence)**: new users' passwords are stored as BCrypt hashes. Existing plaintext entries are still accepted for authentication (no silent migration).
- **Modified rule**: the runtime-dependency restriction from E1 is relaxed to allow the BCrypt library only, as required by F3-C05.

## Capabilities

### New Capabilities
- `layered-architecture`: Package layering, Singleton controllers and startup dependency injection.
- `invoice-generation`: On-demand user and group invoices built with a Builder.
- `password-security`: BCrypt hashing and verification of user passwords.
- `personal-passes`: SpeedPass and Special Pass purchase, refund, persistence and cascade deletion.

### Modified Capabilities
- `project-build`: Runtime dependencies limited to the BCrypt library.
- `command-execution`: `HELP` lists all F3 commands; new `HELP EXTENDED` and injected help generation.
- `attraction-management`: Attraction types and creation syntax, typed and pass-based entry, type in status and persistence.
- `individual-tickets`: `TICKET DELETE` cascades to passes.
- `group-tickets`: `GROUPTICKET DELETE` cascades to constituent passes.
- `user-management`: Password verification against stored hashes in delete/update.
- `user-persistence`: Stored password field holds a BCrypt hash.
- `uml-documentation`: Diagram covers interfaces, generics and pass classes.

## Impact

- All Java files move packages; tests move accordingly. New classes for controllers, help, invoices, passes, attraction subclasses, password hashing.
- `pom.xml`: adds `org.mindrot:jbcrypt:0.4` (runtime) and builds an executable JAR that includes it (shade plugin).
- New data file `passes.txt`; `attractions.txt` gains an optional type column (old 3-field lines load as `GENERAL`); `users.txt` password column holds hashes for new or changed passwords.
- Commit history: the ten mandated milestone commits, followed by the BOTL batch commit that archives the OpenSpec change.
