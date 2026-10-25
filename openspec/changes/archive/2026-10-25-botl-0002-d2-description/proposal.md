# Proposal

## Why

Delivery 2 of the Amusement Park assignment turns the Delivery 1 user-management tool into a park manager: users must be able to form groups, buy individual and group tickets, and use tickets to enter attractions, with all of that state surviving restarts and an isolated `--fly` mode for repeatable testing. Phase 1 behaviour must keep working, except where ticket rules now restrict `USER DELETE`.

## What Changes

- Add a single central park management object (one instance per execution) owning users, groups, tickets, attractions, the current system date and the current base ticket price.
- Add `SYSTEM DATE;<yyyy-MM-dd>` (initially today's local date) and `SYSTEM PRICE;<Price>` (initially 40.00 EUR, not persisted).
- Add user groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`; groups are locked (no add/remove/delete) while an active group ticket exists, derived from tickets and the current date.
- Add a ticket hierarchy sharing one identifier space: individual tickets (`TICKET CREATE`/`TICKET DELETE`) and group tickets (`GROUPTICKET CREATE`/`GROUPTICKET DELETE`) composed of derived individual tickets `<id>:<dni>`, with member-count discounts (0/10/20/50 %). Only tickets dated strictly after the current date can be deleted.
- **BREAKING** (relative to Delivery 1 behaviour): `USER DELETE` is rejected while the user has an active ticket (individual or within a group ticket) or belongs to a locked group; on success the user is also removed from their groups.
- Add attractions: `ATTRACTION CREATE`, `ATTRACTION ENTER` (individual or atomic group entry, capacity-checked, ticket valid for the current date), `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`. Occupancy is not persisted.
- Extend text-file persistence to groups, tickets (including historical records of deleted users/groups) and attractions.
- Add `--fly` temporary persistence mode (`--fly [file]`) using a timestamped directory under `tmp/` that is deleted at the end of execution.
- Extend `HELP` with all new commands.
- Add a PlantUML class model at `uml/model.puml`.

## Capabilities

### New Capabilities
- `park-settings`: Current system date and base ticket price, and the commands that change them.
- `user-groups`: Creating, populating, modifying and deleting user groups, including lock rules derived from active group tickets.
- `ticketing`: Individual and group tickets, shared identifier space, pricing and group discounts, ticket deletion rules and historical retention.
- `attractions`: Attraction definition, ticket-based entry (individual and atomic group entry), exit, listing and status.
- `park-persistence`: Persistence of groups, tickets and attractions across runs, and the `--fly` temporary persistence mode.

### Modified Capabilities
- `application-execution`: Command set and `HELP` output grow to include the Delivery 2 commands; argument handling accepts a leading `--fly`.
- `user-management`: `USER DELETE` is restricted by active tickets and locked groups and removes the user from their groups.

## Impact

- New domain classes (groups, ticket hierarchy, attractions), a central park controller, new file repositories, extended command processor and `App` argument handling.
- New data files next to `users.txt`: `groups.txt`, `tickets.txt`, `attractions.txt`; `--fly` creates and removes `tmp/<yyyyMMdd_HHmmss>/`.
- New `uml/model.puml`. No new runtime dependencies.
- Out of scope: the course's mandatory per-milestone commit roadmap (`[F2-C01]` … `[F2-C10]`); the batch records this delivery as one traceable commit and mirrors the milestones in `tasks.md`.
