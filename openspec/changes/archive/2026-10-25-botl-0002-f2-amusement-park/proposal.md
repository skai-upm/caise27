# Proposal

## Why

Delivery 1 only manages users. The park now needs everything that depends on time and on relationships between objects: a simulated system date and ticket price, user groups, individual and group tickets (with discounts and deletion locks), attractions that visitors enter with their tickets, a PlantUML class diagram documenting the resulting design, and an isolated `--fly` execution mode for running scenarios without touching the persistent data.

## What Changes

- `SYSTEM DATE;yyyy-MM-dd` sets a single application-wide simulated date (initially the OS date at startup, never persisted); every date-dependent rule uses it. `SYSTEM PRICE;<amount>` sets the individual ticket price (initially `40.00` each run, never persisted).
- Groups: `GROUP CREATE;Id;Name`, `GROUP ADD;Id;Email`, `GROUP REMOVE;Id;Email`, `GROUP DELETE;Id`. Groups reference the existing user objects, reject unknown and duplicate members, and persist across runs.
- Individual tickets: `TICKET CREATE;Id;Email;yyyy-MM-dd` (replies `OK TICKET CREATED -> <price>`), `TICKET DELETE;Id` (only for dates after the system date). Tickets reference their user, persist, and past tickets stay as history.
- Group tickets: `GROUPTICKET CREATE;Id;GroupId;yyyy-MM-dd` generates one individual ticket per current member and applies the member-count discount (0% / 10% / 20% / 50%); replies `OK GROUP TICKET CREATED -> <price>`. `GROUPTICKET DELETE;Id` (future dates only) also deletes its individual tickets. Individual and group tickets share a common ticket abstraction.
- Locks driven by the system date: a user with any ticket dated on or after the system date cannot be deleted (**BREAKING** change to Delivery 1 `USER DELETE`); a group with a group ticket dated on or after the system date cannot gain/lose members or be deleted.
- Attractions: `ATTRACTION CREATE;Id;Name;Capacity`, `ATTRACTION DELETE;Id`, `ATTRACTION ENTER;Id;TicketId`, `ATTRACTION EXIT;Id;TicketId`, `ATTRACTION STATUS;Id`. Entry is valid only for tickets dated on the system date, never exceeds capacity, and is atomic for group tickets. Attractions persist; their occupants do not (they start empty each run).
- `uml/model.puml`: a PlantUML class diagram derived from the final implementation.
- `--fly` flag: all persistence files go to a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the application ends; works with interactive and file modes.
- `HELP` lists every new command.

## Capabilities

### New Capabilities
- `system-settings`: Simulated system date and current individual ticket price.
- `user-groups`: Groups of registered users, membership rules, locks and persistence.
- `ticketing`: Individual and group tickets, pricing and discounts, deletion rules, history and persistence.
- `attractions`: Attractions, ticket-based entry/exit, capacity, status query and persistence.
- `uml-model`: The PlantUML class diagram documenting the implemented design.
- `temporary-execution`: The `--fly` isolated persistence mode.

### Modified Capabilities
- `user-management`: `Delete user` is additionally rejected while the user has current or future tickets, and removes the user from their groups.
- `command-interface`: `File mode` takes the commands file from the first argument that is not `--fly`; `HELP` lists the new commands.

## Impact

- New classes for groups, tickets (abstract base with individual and group subclasses), attractions, a main park controller holding the system date and price, and one repository per persisted entity.
- `CommandProcessor` dispatches the new commands to the park controller; `App` handles `--fly`.
- New data files next to `users.txt`: `groups.txt`, `tickets.txt`, `group_tickets.txt`, `attractions.txt`.
- New `uml/model.puml`; `tmp/` added to `.gitignore`.
- No new runtime dependencies.
