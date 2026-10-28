# Proposal

## Why

Milestone F2-C03 introduces tickets, needed to access attractions on a given date. It adds the common ticket abstraction shared by individual and (later) group tickets, individual ticket purchase with `TICKET CREATE` at the current system price, deletion of future tickets with `TICKET DELETE`, and makes `USER DELETE` respect active tickets.

## What Changes

- Add a common ticket abstraction (identifier, date of use, price; active/deletable checks relative to the current system date) and the individual ticket as a concrete type referencing its user (unidirectional: users do not reference tickets).
- Add `TICKET CREATE;<identifier>;<email>;<date>`: identifier unique in the shared ticket identifier space (checked in one common place), existing user, date ≥ current system date, price = current base price; prints the price charged.
- Add `TICKET DELETE;<identifier>`: allowed only for tickets dated strictly after the current system date; tickets for today or earlier are kept as historical records; individual tickets belonging to a group ticket composition cannot be deleted this way (group tickets arrive in F2-C04).
- `USER DELETE` is rejected while the user has an active ticket (date ≥ current system date) or belongs to a group locked by an active group ticket; the lock is derived each time from the tickets, never stored. Historical tickets keep their user reference after the user is deleted.
- `USER UPDATE` now modifies the existing user object (instead of replacing it) so tickets keep referencing the same, up-to-date user.
- `HELP` lists the ticket commands.
- Commit with the mandatory message `[F2-C03] Add individual tickets`.

## Capabilities

### New Capabilities
- `tickets`: Common ticket abstraction, shared identifier space, individual tickets and their creation/deletion rules.

### Modified Capabilities
- `user-deletion`: deletion blocked by active tickets of the user and by membership in a locked group.
- `execution-control`: `HELP` also lists `TICKET CREATE` and `TICKET DELETE`.

## Impact

- New `model.Ticket` (abstract), `model.IndividualTicket`, `repository.TicketRepository`, `service.TicketService`; `ParkController` owns tickets and orchestrates `USER DELETE`; `UserService.updateUser` mutates in place.
- Tickets are not persisted yet (F2-C08).
- Ambiguities flagged:
  - Output on creation: `TICKET CREATED. Price: <amount> EUR` (amount with two decimals); deletion: `TICKET DELETED`.
  - Ticket identifiers must be non-empty; dates use the same strict `yyyy-MM-dd` format as `SYSTEM DATE`.
  - The group-lock part of `USER DELETE` becomes observable once group tickets exist (F2-C04); it is wired now through the derived group lock.
