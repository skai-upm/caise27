# Proposal

## Why

Milestone F2-C03 introduces tickets: a common ticket abstraction shared by every ticket type, individual tickets bought with `TICKET CREATE` at the current system price, deletion of future tickets with `TICKET DELETE`, and the rule that a user with an active ticket cannot be deleted.

## What Changes

- Add a common ticket abstraction (identifier, date of use, price, active/deletable rules, "involves user" query) reused by every ticket type.
- Add individual tickets referencing their user (unidirectional: users hold no tickets).
- Keep all tickets in one shared identifier space owned by the central controller, checked in one common place.
- `TICKET CREATE;<identifier>;<email>;<date>`: unique identifier, existing user, date ≥ current system date; price = current base price; the response shows the price.
- `TICKET DELETE;<identifier>`: only when the ticket date is strictly after the current system date; today's and past tickets are kept as historical records.
- `USER DELETE` is rejected while the user has an active ticket (date ≥ current system date), derived from the tickets each time, never stored on the user.
- Commit with the exact message `[F2-C03] Add individual tickets`.

## Capabilities

### New Capabilities
- `tickets`: Common ticket abstraction, shared identifier space, individual tickets and the TICKET CREATE/DELETE commands.

### Modified Capabilities
- `user-deletion`: USER DELETE additionally requires that the user has no active ticket.

## Impact

- New `model/Ticket` (abstract), `model/IndividualTicket`, `repository/TicketRepository`; `ParkController` gains tickets and the active-ticket query; `CommandProcessor` gets TICKET commands (new `TicketCommands`) and the USER DELETE check; HELP lists the commands.
- Out of scope here (F2-C04): group tickets, derived identifiers `<groupTicket>:<dni>`, discounts, protection of individual tickets inside a group ticket, group locking rules. Ticket persistence is F2-C08.
- Ambiguities flagged: success response is `TICKET CREATED - Price: <amount> EUR` (amount with two decimals); identifiers are compared exactly; an unknown ticket in TICKET DELETE is an error.
