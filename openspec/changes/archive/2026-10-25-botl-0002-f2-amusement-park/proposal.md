# Proposal

## Why

Delivery 1 left a working user-management application. Delivery 2 (F2) turns it into a park system: a simulated system date for testable time rules, user groups, individual and group tickets with pricing and discounts, attractions with capacity-controlled access, a PlantUML class model documenting the implementation, and an isolated `--fly` execution mode so runs and tests do not alter the permanent data.

## What Changes

- `SYSTEM DATE;yyyy-MM-dd` sets a single application-wide current date (initially the operating system date at startup, not persisted). Every time-dependent rule uses it.
- `SYSTEM PRICE;<amount>` sets the individual ticket price (40.00 at every start, not persisted).
- Groups of existing users: `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`; groups reference the existing user objects and persist between runs.
- Individual tickets: `TICKET CREATE;id;email;date` (answers `OK TICKET CREATED -> <price>`) and `TICKET DELETE;id` (only for dates later than the system date). Tickets persist and past tickets remain as history.
- Group tickets: `GROUPTICKET CREATE;id;group;date` generates one individual ticket per current member and applies the member-count discount (0 % / 10 % / 20 % / 50 %); `GROUPTICKET DELETE;id` removes it and its internal tickets when it is in the future. Individual and group tickets share a common ticket abstraction.
- **BREAKING (rule change)**: `USER DELETE` is rejected while the user holds a ticket dated on or after the system date. A group with a group ticket dated on or after the system date is locked (no add/remove/delete).
- Attractions: `ATTRACTION CREATE;id;name;capacity`, `ATTRACTION DELETE;id`, `ATTRACTION ENTER;id;ticket`, `ATTRACTION EXIT;id;ticket`, `ATTRACTION STATUS;id`. Entry works uniformly for individual and group tickets, requires the ticket date to equal the system date, never exceeds capacity and is atomic for groups. Attractions persist; their occupants do not.
- `uml/model.puml`: PlantUML class diagram of the current implementation following the given notation rules.
- `--fly` option: all persistence files go to a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the run ends; works in interactive and file modes.
- `HELP` lists every new command.

## Capabilities

### New Capabilities

- `system-settings`: Simulated current system date and the configurable individual ticket price.
- `group-management`: Groups of existing users, their membership rules, locking and persistence.
- `ticket-management`: Individual tickets — purchase, pricing, deletion rules, history and persistence.
- `group-tickets`: Group tickets composed of per-member individual tickets, discounts, deletion and locking effects.
- `attractions`: Attractions, ticket-based entry and exit, capacity control and status queries.
- `uml-model`: The PlantUML class diagram documenting the implemented design.
- `temporary-execution`: The `--fly` mode that redirects persistence to a disposable timestamped directory.

### Modified Capabilities

- `user-management`: User deletion is blocked by current or future tickets and removes the user's remaining (past) tickets and group memberships.
- `command-processing`: HELP lists the new commands.
- `execution-modes`: The command-file argument may be combined with the `--fly` option.

## Impact

- New domain, controller and repository classes in `es.upm.parque`; `CommandProcessor` gains the new commands; `App` parses `--fly` and builds storage paths.
- New data files under `data/` (groups, tickets, group tickets, attractions) and `tmp/` for `--fly` runs (both Git-ignored).
- New `uml/model.puml`.
- No new runtime dependencies.
