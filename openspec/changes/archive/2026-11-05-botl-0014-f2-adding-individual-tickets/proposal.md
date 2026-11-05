# Proposal

## Why

Milestone F2-C03 (Delivery 2, step 3) adds the common ticket abstraction and individual tickets. `TICKET CREATE` uses the current system price and rejects dates earlier than the current date. `TICKET DELETE` deletes only tickets dated strictly after the current date. From now on, `USER DELETE` must refuse to delete a user who has a ticket dated on or after the current system date (source §7, §7.1, §7.2, §9). Today there are no tickets, and `USER DELETE` ignores them.

## What Changes

- Introduce a common ticket abstraction (identifier, date of use, price) that every ticket type extends and that operations can use without knowing the concrete type. A ticket is *active* when its date is on or after the current system date, and *historical* otherwise.
- Introduce individual tickets, which hold a one-way reference to their user. Users never store tickets.
- Keep all tickets in one store with a single shared identifier space, so uniqueness is checked once for every ticket type.
- Add `TICKET CREATE;<identifier>;<email>;<date>`: the identifier must be unique and non-empty, the user must exist, and the date must be valid and not earlier than the system date. The price is the current base price, and the response shows that price.
- Add `TICKET DELETE;<identifier>`, allowed only for an individual ticket dated strictly after the system date. Tickets for today or earlier are kept as historical records. Tickets that belong to a group ticket's composition cannot be deleted directly; that rule becomes reachable with group tickets in the next milestone.
- Extend `USER DELETE`: it is rejected while the user has an active ticket, determined by traversing the system's tickets rather than from a stored flag. It is also rejected while the user belongs to a group locked by an active group ticket. Historical tickets keep their user reference after deletion.
- List the ticket commands in `HELP`.

In scope: the ticket abstraction, individual tickets, the shared identifier space, `TICKET CREATE`/`DELETE`, and the `USER DELETE` restrictions.

Out of scope: group tickets and their discount (next milestone), and ticket persistence ("complete park persistence" milestone).

## Capabilities

### New Capabilities
- `tickets`: The ticket abstraction, shared identifier space, active versus historical tickets, and individual ticket creation and deletion.

### Modified Capabilities
- `user-deletion`: adds the restrictions that a user with an active ticket, or who belongs to a group locked by an active group ticket, cannot be deleted.

## Impact

- New `model.Ticket` (abstract), `model.IndividualTicket`, `repository.TicketRepository`, `command.TicketCommands`; `ParkController` holds the tickets and the derived queries; the `USER DELETE` handler and `HELP` are updated; tests.
