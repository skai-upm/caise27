# Proposal

## Why

Milestone F2-C03 introduces tickets: a common ticket abstraction shared by all ticket types, individual ticket purchase with `TICKET CREATE` at the current system price, deletion of future tickets with `TICKET DELETE`, and the rule that a user with an active ticket cannot be deleted.

## What Changes

- Common ticket abstraction (unique identifier in one shared identifier space, date of use, price) that group tickets (F2-C04) will extend.
- `TICKET CREATE;<identifier>;<email>;<date>`: existing user, unique identifier, date not earlier than the system date, price = current base price; shows the price charged.
- `TICKET DELETE;<identifier>`: only individual tickets dated strictly after the system date (and not part of a group ticket); others stay as historical records.
- `USER DELETE` is rejected while the user has an individual ticket dated on or after the system date; historical tickets keep their user data.
- HELP lists `TICKET CREATE` and `TICKET DELETE`.
- Record the mandatory milestone commit `[F2-C03] Add individual tickets` once verified.

## Capabilities

### New Capabilities
- `ticket-management`: Park tickets — common ticket properties and identifier space, individual ticket purchase and deletion, active vs historical tickets.

### Modified Capabilities
- `user-management`: `USER DELETE` additionally requires that the user has no active ticket.
- `command-processing`: HELP lists the TICKET commands.

## Impact

- New `Ticket` (abstract), `IndividualTicket`, `TicketRepository`, `TicketService`, `TicketOperationException`; `ParkController` gives access to tickets; `UserService.deleteUser` checks tickets via the controller.
- Ticket persistence belongs to F2-C08.
