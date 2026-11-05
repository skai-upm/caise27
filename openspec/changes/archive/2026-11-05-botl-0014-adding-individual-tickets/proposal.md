# Proposal

## Why

Milestone F2-C03 introduces tickets, which users need to access attractions. It requires a common ticket abstraction (shared by the individual tickets added now and the group tickets of F2-C04), individual ticket purchase with `TICKET CREATE` at the current system price, `TICKET DELETE` for future tickets, and a stricter `USER DELETE` that refuses to delete users with active tickets.

## What Changes

- Common ticket abstraction: unique identifier in a single identifier space shared by all ticket types (checked in one place), date of use, and price behavior.
- `TICKET CREATE;<identifier>;<email>;<date>`: existing user, date ≥ current system date, price = current base price; prints the price charged.
- `TICKET DELETE;<identifier>`: only when the ticket date is strictly after the current system date; today's and past tickets are kept as history.
- `USER DELETE` is rejected while the user has an active ticket (date ≥ current date); historical tickets do not block it and are kept, still referring to the user's data.
- The ticket knows its user; users do not reference tickets — a user's tickets are found by traversing the system's tickets.
- Out of scope: group tickets and their effects on groups (F2-C04), attraction access (F2-C06), ticket persistence (F2-C08).

## Capabilities

### New Capabilities
- `tickets`: The ticket abstraction, the shared identifier space, and individual ticket creation and deletion.

### Modified Capabilities
- `user-deletion`: adds the rule that users with active tickets cannot be deleted.

## Impact

- New `model.Ticket` (abstract), `model.IndividualTicket`, `repository.TicketRepository`, `service.TicketService`; `Park` exposes tickets; `UserService.deleteUser` checks tickets; new commands and HELP lines.
