# Proposal

## Why

Delivery 2 of the Amusement Park assignment (source `2.F2 - Amusement Park.md`, milestones F2-C01 … F2-C10) extends the Delivery 1 user-management application into a park: a single central controller with a system date and ticket price, user groups, individual and group tickets with discounts, attractions with capacity-controlled access, persistence of all of this, a temporary `--fly` execution mode and a PlantUML model of the design. Delivery 1 behaviour must keep working.

## What Changes

- **F2-C01** — Add a single central park-management object (one instance per execution, guaranteed by design) that owns users, groups, tickets and attractions plus the current system date (initially the local date at start-up) and base ticket price (initially 40.00 €). Add `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`; neither value is persisted.
- **F2-C02** — Add groups: `GROUP CREATE;Identifier;Name`, `GROUP ADD;Identifier;Email`, `GROUP REMOVE;Identifier;Email`. No duplicate members. A group is *locked* (derived, never stored) while any associated group ticket is active (date ≥ system date); add/remove are rejected while locked.
- **F2-C03** — Add a common ticket abstraction (inheritance) with a shared identifier space. `TICKET CREATE;<identifier>;<email>;<date>` (date ≥ system date, current price, price displayed) and `TICKET DELETE;<identifier>` (only if date > system date, never for tickets inside a group ticket). Individual tickets reference their user; users never reference tickets.
- **F2-C04** — `GROUPTICKET CREATE;<identifier>;<group>;<date>` builds a group ticket *composed* of one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`, all identifiers checked before anything is created, discount by member count (<3: 0 %, 3–5: 10 %, 6–10: 20 %, >10: 50 %) applied to each individual ticket, total displayed. `GROUPTICKET DELETE;<identifier>` (date > system date; deletes the composition). `GROUP DELETE;Identifier` (rejected while an active group ticket exists; historical group tickets keep their data and lose the group reference). **BREAKING (behavioural extension):** `USER DELETE` is now rejected while the user has any active ticket (standalone or inside a group ticket) or belongs to a locked group; when permitted, the user is also removed from their groups.
- **F2-C05** — Add attractions (identifier, name, capacity, occupants): `ATTRACTION CREATE;Identifier;Name;Capacity`, `ATTRACTION LIST;AttractionIdentifier` (read-only, emails of occupants).
- **F2-C06 / F2-C07** — `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` through the common ticket abstraction: ticket must exist and be dated today; an individual ticket admits its user, a group ticket admits all composition users atomically (all or none, capacity respected). `ATTRACTION EXIT;AttractionIdentifier;Email`. `ATTRACTION STATUS;AttractionIdentifier` shows identifier, name, capacity, occupants count, free places and occupants.
- **F2-C08** — Persist groups (with members), tickets (individual with user; group with composition and, while it exists, group) and attractions in text files alongside `users.txt`; occupants are not persisted (attractions start empty). No Java serialization. Add `--fly [file]`: all repositories point to a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when execution ends (EXIT, end of file, or end of input). Without `--fly`, argument behaviour is unchanged.
- **F2-C09** — Add `uml/model.puml`, a PlantUML class diagram consistent with the code (ticket hierarchy, composition, associations with directions and multiplicities, enums).
- **F2-C10** — Integrate everything; extend `HELP` with the Delivery 2 syntax list exactly as given in the source.

### Flagged ambiguities (recorded, not silently resolved)

1. **Mandatory milestone commits.** The source requires ten separate commits (`[F2-C01] …` – `[F2-C10] …`). The batch orchestrator mandates one commit per proposal file; the messages are listed in that commit's body for traceability, and the separate milestone commits must be reconciled by the course owner.
2. **SYSTEM PRICE format.** "Valid, positive monetary amount" is interpreted as: digits with an optional `.` and at most two decimals, strictly greater than zero (e.g. `45.50`, `45`, `0.01`). Signs, exponents, commas and more than two decimals are rejected.
3. **Date format.** `yyyy-MM-dd` is parsed strictly (`2027-02-30` and `2027-4-15` are rejected). `SYSTEM DATE` may move the date backwards or forwards without restriction.
4. **Identifiers and names.** Group, ticket and attraction identifiers must be non-empty; group/attraction names are stored as given. Attraction capacity must be a positive integer.
5. **Re-entry.** A user already inside an attraction cannot enter the same attraction again (the whole entry is rejected). The source does not forbid being inside several attractions at once, so that is not restricted.
6. **Composition tickets at attractions.** A composition ticket (`TG01:12345678Z`) is an individual ticket in the shared space, so it can be used with `ATTRACTION ENTER` to admit that one user.
7. **Inactive users at attractions.** If `SYSTEM DATE` is moved back so that a ticket of an already-deleted user becomes "today" again, entry is rejected because the user is no longer an active system user.
8. **USER DELETE of a user inside an attraction.** Not specified; the user is removed from the attraction occupants as part of the deletion, to keep the model consistent (analogous to group removal).
9. **Wrong ticket kind.** `TICKET DELETE` on a group ticket identifier and `GROUPTICKET DELETE` on an individual ticket identifier are rejected with an explanatory error.
10. **Messages.** Output wording is free per source; success messages are short upper-case confirmations (e.g. `TICKET CREATED: price 40.00 EUR`, `GROUPTICKET CREATED: total 192.00 EUR`), errors start with `ERROR:`.
11. **HELP wording.** Delivery 2 explicitly redefines the minimum HELP syntax (e.g. `<FirstName>` instead of Delivery 1's `<firstName>`); the Delivery 2 text is adopted.
12. **Rounding.** Discounted per-ticket prices are rounded to cents (half-up); the group total is the sum of the rounded individual prices.
13. **Storage files and `--fly` naming.** Normal mode stores `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt` in the working directory. If `tmp/<timestamp>/` already exists, a numeric suffix is appended; the `tmp/` parent is removed too if it was empty and created by this execution.
14. **Duplicated source sections** (7.1/7.2, 13.1 appear twice) are treated as one requirement each.

## Capabilities

### New Capabilities

- `park-management`: Single central park controller, current system date and current base ticket price (`SYSTEM DATE`, `SYSTEM PRICE`).
- `user-groups`: Group creation, membership changes, derived lock state and group deletion.
- `tickets`: Common ticket abstraction, shared identifier space, individual tickets, group tickets with composition and discounts, deletion and historical records.
- `attractions`: Attraction creation, capacity-controlled entry with individual or group tickets, exit, list and status.
- `park-persistence`: Persistence of groups, tickets and attractions and the `--fly` temporary execution mode.
- `design-model`: The PlantUML class model `uml/model.puml`.

### Modified Capabilities

- `user-management`: `Delete user` now also respects active tickets and locked groups and removes the user from groups (and attractions).
- `command-interface`: `Command syntax` recognises the Delivery 2 commands; `HELP command` shows the Delivery 2 syntax list.
- `user-persistence`: `Text file format` stores `users.txt` in the configured data directory (working directory normally, temporary directory under `--fly`).

## Impact

- New packages/classes for park, groups, tickets, attractions and their repositories; `CommandProcessor` and `App` extended; `UserService` becomes an in-memory registry used by the park controller, which now owns persistence.
- New runtime files `groups.txt`, `tickets.txt`, `attractions.txt`, and transient `tmp/` (all git-ignored).
- New `uml/model.puml`.
