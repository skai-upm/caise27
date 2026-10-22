# Proposal

## Why

Delivery 2 of the Amusement Park assignment turns the user-management foundation from Delivery 1 into an actual park: users form groups, buy individual or discounted group tickets for a date, and use those tickets to enter attractions with limited capacity. Park operations now depend on a configurable system date and ticket price, and the extended state must survive restarts, with an isolated temporary mode for repeatable test runs.

## What Changes

- Add a single central park-management object owning the system date (initially today, changed with `SYSTEM DATE;<yyyy-MM-dd>`) and the base ticket price (initially 40.00, changed with `SYSTEM PRICE;<Price>`, not persisted).
- Add user groups: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`; a group is locked while it has an active group ticket (date ≥ system date), derived from tickets, not stored.
- Add tickets with a common abstraction and a shared identifier space: `TICKET CREATE`/`TICKET DELETE` for individual tickets and `GROUPTICKET CREATE`/`GROUPTICKET DELETE` for group tickets composed of one individual ticket per member (`<groupTicketId>:<dni>`), with member-count discounts (0/10/20/50 %). Only tickets dated strictly after the system date can be deleted.
- Add attractions: `ATTRACTION CREATE`, `ATTRACTION ENTER` (individual or atomic group entry within capacity, ticket must be for the system date), `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`.
- **BREAKING (behavior)**: `USER DELETE` is now rejected while the user has an active ticket or belongs to a locked group; on deletion the user is removed from their groups.
- Persist groups, tickets (including historical tickets of deleted users/groups) and attractions in text files; attraction occupants are not persisted.
- Add `--fly` temporary mode: all data files live in a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted at the end of the execution.
- Extend `HELP` with all new commands and add a PlantUML class diagram at `uml/model.puml`.

## Capabilities

### New Capabilities
- `park-state`: System date and base ticket price that date- and price-dependent operations use.
- `user-groups`: Creating, modifying and deleting groups of users, and the ticket-derived lock on groups.
- `tickets`: Individual and group tickets, shared identifier space, pricing and discounts, creation and deletion rules.
- `attractions`: Attraction creation, entry with tickets, exit, occupant listing and status.
- `park-persistence`: Storage and reconstruction of groups, tickets (including historical records) and attractions.

### Modified Capabilities
- `user-management`: `User deletion` gains ticket- and group-based restrictions and removes the user from their groups.
- `user-persistence`: `Text file storage format` stores `users.txt` in the active data directory (working directory, or the temporary directory in `--fly` mode).
- `command-interface`: `Command syntax` lists the new commands; `HELP command` must show all of them.
- `application-runtime`: adds the `--fly` temporary persistence mode.

## Impact

- Code: new domain classes (groups, tickets, attractions, central park object), new repositories/stores, new services, a registry-based command processor, `App` argument handling for `--fly`.
- Data files in the data directory: `users.txt` (unchanged format), new `groups.txt`, `tickets.txt`, `attractions.txt`; `--fly` uses `tmp/<timestamp>/`.
- New documentation artifact: `uml/model.puml`.
- Still Java standard library only at runtime (no serialization, databases or frameworks).
- Out of scope: the course's mandatory `[F2-C01]`…`[F2-C10]` student commit roadmap (commits here are governed by the batch workflow).
