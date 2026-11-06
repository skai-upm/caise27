# Proposal

## Why

Milestone F2-C03 (Delivery 2, step 3) introduces tickets: a common ticket abstraction shared by all ticket types, individual tickets created with `TICKET CREATE` at the current system price for dates not earlier than the current date, and `TICKET DELETE` for tickets dated strictly after the current date. From now on `USER DELETE` must respect the user's active tickets and the locking of their groups (source section 9).

## What Changes

- Common ticket abstraction (identifier, date of use, price; active/historical status) with individual tickets as the first concrete type; a single identifier space checked in one place for all ticket types.
- `TICKET CREATE;<identifier>;<email>;<date>`: unique identifier, existing user, date ≥ current date, price = current base price; prints the price charged. The ticket references its user; users never reference tickets.
- `TICKET DELETE;<identifier>`: only when the ticket date is strictly after the current date; tickets for today or earlier are kept as historical records.
- `USER DELETE` is rejected while the user has an active ticket (date ≥ current date) or belongs to a locked group; when allowed, the user is also removed from their groups. Historical tickets are kept and still reference the user's data.
- `USER UPDATE` keeps the same user object (changes applied in place after persisting) so tickets always see current user data.
- `HELP` lists the ticket commands.

### In scope
- Source sections 7 (common part), 7.1, 7.2 and 9.

### Out of scope
- Group tickets, `GROUPTICKET CREATE/DELETE`, group-ticket composition and discounts (F2-C04); ticket persistence (F2-C08).

## Capabilities

### New Capabilities
- `tickets`: the common ticket abstraction, the shared identifier space, individual ticket creation and deletion, and active/historical status.

### Modified Capabilities
- `user-deletion`: deletion is blocked by active tickets or locked groups, and removes the user from their groups.
- `execution-control`: `HELP` lists `TICKET CREATE` and `TICKET DELETE`.

## Impact

- New `Ticket` (abstract), `IndividualTicket`, `TicketRepository`, `TicketService`, `TicketException`; `ParkController` orchestrates user deletion across users, groups and tickets; `UserRepository.update` replaces `replace`; `User.copy()`.
