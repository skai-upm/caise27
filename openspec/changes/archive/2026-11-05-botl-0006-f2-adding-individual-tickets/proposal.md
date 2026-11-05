# Proposal

## Why

Milestone F2-C03 introduces tickets: a common ticket abstraction (identifier, date of use, price) shared by all ticket types, individual ticket purchase with `TICKET CREATE` at the current base price, and `TICKET DELETE` restricted to future tickets. It also makes active tickets block user deletion. Access control and group tickets build on this.

## What Changes

- Add a common ticket abstraction (unique identifier, date, price behavior) with a single identifier space checked in one place for every ticket type.
- Add individual tickets bound to one existing user: `TICKET CREATE;<identifier>;<email>;<date>` — unique id, existing user, date ≥ current system date, price = current base price, displayed on success.
- Add `TICKET DELETE;<identifier>` — only when the ticket date is strictly after the current system date; today/past tickets are kept as historical records.
- Add the derived rule "a user with an active ticket (date ≥ current system date) cannot be deleted" as a central-management guard, ready for user deletion.
- `HELP` lists the TICKET commands.

### In scope
- Ticket abstraction, individual tickets, ticket deletion rule, user-deletion guard based on individual tickets.

### Out of scope / deferred
- Group tickets, derived identifiers and discounts (next milestone; the abstraction is designed for them).
- The `USER DELETE` command and the user model are not yet in the code base (batch ordering). The ticket refers to its user through the user's e-mail resolved via central management; replacing it with a direct object reference is deferred to the user-model milestone. The guard is exposed for `USER DELETE` to call when it is implemented.

## Capabilities

### New Capabilities
- `tickets`: Ticket abstraction shared by all ticket types, common identifier space, individual ticket creation and deletion, and how active tickets constrain user deletion.

### Modified Capabilities
<!-- None. -->

## Impact

- New `Ticket` (abstract), `IndividualTicket`, `TicketCommands`; `ParkController` gains ticket storage and guards; `HELP`; tests.
