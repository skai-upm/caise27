# Proposal

## Why

F1 delivered user management, text-file persistence and shared interactive/file command execution. Delivery 2 (F2) turns this into a park operations tool: time-dependent rules driven by a configurable system date, user groups, individual and group tickets with discounts, attractions with capacity-controlled access, a PlantUML model of the real implementation, and an isolated `--fly` mode for tests. These pieces interact (tickets lock users and groups, attractions consume tickets), so they are delivered as one coherent change while preserving all F1 behavior.

## What Changes

- **System date and price**: one runtime system date (initialized to the OS local date) changed with `SYSTEM DATE;yyyy-MM-dd`, and one runtime base ticket price (initialized to `40.00`) changed with `SYSTEM PRICE;<price>`. Neither is persisted. All date rules use the system date.
- **Groups**: `GROUP CREATE;<id>;<name>`, `GROUP ADD;<id>;<email>`, `GROUP REMOVE;<id>;<email>`, `GROUP DELETE;<id>`; groups reference existing users, reject unknown or duplicate members, and persist.
- **Individual tickets**: `TICKET CREATE;<id>;<email>;<date>` → `OK TICKET CREATED -> <Precio>`; `TICKET DELETE;<id>` only for dates strictly after the system date. Tickets persist with their purchase price and remain as historical records.
- **Group tickets**: `GROUPTICKET CREATE;<id>;<groupId>;<date>` → `OK GROUP TICKET CREATED -> <Precio>` composes one individual ticket per current member, with size-based discounts (0/10/20/50 %). `GROUPTICKET DELETE;<id>` for future dates deletes the whole composition. Groups with a current or future group ticket are locked.
- **Attractions**: `ATTRACTION CREATE;<id>;<name>;<capacity>`, `ATTRACTION ENTER;<id>;<ticketId>` (individual or group ticket, valid only on the system date, atomic for groups, capacity-bound), `ATTRACTION EXIT;<id>;<ticketId>` (no date check), `ATTRACTION STATUS;<id>`. Definitions persist; occupancy starts empty on every run.
- **User deletion (modified)**: `USER DELETE` is rejected while the user has any ticket dated on or after the system date; a deleted user is removed from groups, and historical tickets keep the user's identifying data.
- **Command execution (modified)**: new commands registered in the shared processor and `HELP`; optional `--fly` argument before the optional command file.
- **Temporary persistence**: `--fly` routes every repository to `tmp/<yyyyMMdd_HHmmss>/` and deletes it on termination.
- **UML**: `uml/model.puml` generated from the final code.

Out of scope: F3 features (SpeedPass, Special Pass, invoice builder, BCrypt, attraction categories), Singleton as a requirement, databases, web/API interfaces.

## Capabilities

### New Capabilities
- `system-settings`: Runtime system date and base ticket price, their commands and initial values.
- `group-management`: Groups of existing users, membership rules, locking by active group tickets, and group persistence.
- `individual-tickets`: Dated individual tickets with stored purchase price, refund (deletion) rules, historical retention and persistence.
- `group-tickets`: Group ticket composition, discount brackets, atomic creation/deletion and persistence.
- `attraction-management`: Attraction definitions and persistence, ticket-based atomic entry, exit and status.
- `uml-documentation`: PlantUML class diagram reflecting the implemented code.
- `temporary-persistence`: `--fly` isolated storage directory and cleanup.

### Modified Capabilities
- `user-management`: `Delete user` additionally rejects users with current/future tickets and removes deleted users from groups.
- `command-execution`: `File mode` and `Interactive mode` accept the `--fly` flag; `HELP command` lists the F2 commands.

## Impact

- Code: new domain classes (groups, ticket hierarchy, attractions), services and repositories; `App`/`CommandProcessor` extended; a park controller holding the system date and price.
- Data files (normal mode, working directory): existing `users.txt` unchanged in format; new `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`.
- New `uml/model.puml`; `tmp/` added to `.gitignore`.
- No new runtime dependencies.
- Open interpretation decisions (attraction exit identifier, UML method display, constituent ticket IDs, historical references) are recorded in design.md.
