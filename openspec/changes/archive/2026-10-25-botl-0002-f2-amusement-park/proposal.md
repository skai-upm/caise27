# Proposal

## Why

Delivery 1 left a working user-management application. Delivery 2 (F2) turns it into an amusement-park system: a simulated system date for testable time rules, user groups, individual and group tickets with pricing and discounts, attractions with ticket-based access and status queries, a PlantUML model documenting the design, and a `--fly` mode for isolated runs that never touch permanent data.

## What Changes

- Add `SYSTEM DATE;yyyy-MM-dd`: a single simulated current date held by the main controller, initialised to the OS date on each run, freely movable backward/forward, not persisted. All time-dependent rules use it.
- Add groups of existing users: `GROUP CREATE;id;name`, `GROUP ADD;id;email`, `GROUP REMOVE;id;email`, `GROUP DELETE;id`. Groups reference the same user objects (no copies), reject unknown users and duplicates, and persist in a file.
- Add individual tickets: `TICKET CREATE;id;email;date` (returns `OK TICKET CREATED -> <price>`), `TICKET DELETE;id` (only for dates after the system date), and `SYSTEM PRICE;amount` (non-persisted price, default 40.00). Tickets persist and past tickets are kept as history.
- Add group tickets: `GROUPTICKET CREATE;id;groupId;date` generates one individual ticket per current member and applies the member-count discount (0% / 10% / 20% / 50%); `GROUPTICKET DELETE;id` removes it and its internal tickets when its date is in the future. A group with a current or future group ticket is locked.
- **BREAKING (rule change)**: `USER DELETE` is rejected while the user has any ticket (individual or internal to a group ticket) dated on or after the system date; a deleted user is also removed from the groups it belonged to.
- Add attractions: `ATTRACTION CREATE;id;name;capacity`, `ATTRACTION DELETE;id`, `ATTRACTION ENTER;id;ticketId`, `ATTRACTION EXIT;id;ticketId`, `ATTRACTION STATUS;id`. Entry is valid only on the ticket's date, never exceeds capacity, and group entry is atomic. Attractions persist; occupancy starts empty on each run.
- Add `uml/model.puml`, a PlantUML class diagram derived from the actual code, following the stated notation rules.
- Add `--fly` mode: persistence files live in a timestamped `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the run ends; works with interactive and file modes.
- Extend `HELP` with every new command.

## Capabilities

### New Capabilities
- `system-date`: The single simulated current date and the `SYSTEM DATE` command.
- `user-groups`: Groups of existing users, their commands, rules and persistence.
- `ticketing`: Individual tickets, the configurable ticket price (`SYSTEM PRICE`), ticket deletion rules and ticket persistence.
- `group-ticketing`: Group tickets composed of individual tickets, discount brackets, group locking and group-ticket persistence.
- `attractions`: Attractions, ticket-based entry/exit, capacity rules, status queries and attraction persistence.
- `design-model`: The PlantUML class diagram documenting the implemented design.
- `temporary-execution`: The `--fly` execution mode with temporary, self-deleting persistence.

### Modified Capabilities
- `command-processing`: HELP lists the new commands; the file-mode argument rules change to allow the `--fly` option.
- `user-management`: Deleting a user is blocked by current or future tickets and removes the user from its groups.

## Impact

- Code in `src/main/java/es/upm/parque/`: new domain, manager and repository classes; `App`, `CommandProcessor` and `UserManager` updated; a main controller introduced.
- New data files next to `data/users.txt`: groups, tickets, group tickets, attractions.
- New `uml/model.puml`; `tmp/` added to `.gitignore`.
- No new runtime dependencies (standard library only).
