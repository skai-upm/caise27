# Proposal

## Why

Milestone F2-C03 adds tickets, which users need in order to access attractions. It requires a common ticket abstraction that every ticket type shares, plus individual tickets. Individual tickets are created with `TICKET CREATE` at the current system price and are never dated in the past, and `TICKET DELETE` deletes them only when strictly future. From this milestone on, deleting a user must take that user's active tickets into account.

## What Changes

- Add a common ticket abstraction with unique identifier, date of use and price, shared by every ticket type, and a single ticket identifier space managed by the central park controller. Uniqueness is checked in one common place for all ticket types.
- Add individual tickets associated with one existing user. The relationship is one-way: the ticket knows the user, and users never store tickets.
- Add `TICKET CREATE;<identifier>;<email>;<date>`. The identifier must be unused, the user must exist and the date must not be earlier than the current system date. The price is the current base price at creation, and the command displays the price charged.
- Add `TICKET DELETE;<identifier>`, allowed only for an individual ticket dated strictly after the current system date. Tickets for today or earlier are kept as historical records.
- Add the user-deletion constraint. A user cannot be deleted while any individual ticket of theirs is active (dated today or later). This is derived from the stored tickets and the current date and never kept as user state.
- List the TICKET commands in HELP.

### In scope
- Ticket abstraction, individual tickets, TICKET CREATE/DELETE, and the active-ticket check used for user deletion.

### Out of scope
- Group tickets, the `<groupTicket>:<dni>` composition and GROUPTICKET commands (F2-C04). The abstraction and shared identifier space are designed for them.
- The `USER DELETE` command itself and the user model (Delivery 1 user milestones, which the batch processes later). This change provides the deletion check that USER DELETE must use: active-ticket check, locked-group check and removal from groups.
- Persistence of tickets (complete park persistence milestone).

## Capabilities

### New Capabilities
- `tickets`: the ticket abstraction, shared identifier space, individual tickets with their creation and deletion rules, and the ticket-derived constraints on deleting users.

### Modified Capabilities
- None. Group rules in `user-groups` are unchanged. Group tickets will feed the existing lock rule in a later milestone.

## Impact

- New `es.upm.park.model.Ticket` (abstract) and `IndividualTicket`.
- `ParkController`: ticket storage, ticket operations and the user-deletion check.
- `CommandProcessor`: TICKET CREATE/DELETE and HELP.
- New unit tests.
