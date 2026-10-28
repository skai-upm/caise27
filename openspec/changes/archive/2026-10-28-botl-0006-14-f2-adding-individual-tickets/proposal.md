# Proposal

## Why

Milestone F2-C03 introduces tickets: a common ticket abstraction shared by all ticket types, individual tickets created with `TICKET CREATE` at the current system price (rejecting past dates), deletion of strictly future tickets with `TICKET DELETE`, and the rule that a user with an active ticket (dated on or after the current system date) cannot be deleted. Group tickets (F2-C04) and attraction access depend on this abstraction.

## What Changes

- Add a common ticket abstraction (unique identifier, date of use, price) reused by every ticket type, and the individual ticket type referencing exactly one user (unidirectional: users hold no ticket references).
- Add a single ticket identifier space managed by the park controller, checked in one common place for all ticket types, with all-or-nothing registration of several tickets (needed later by group tickets).
- Add `TICKET CREATE;<identifier>;<email>;<date>`: unique identifier, existing user, date ≥ current system date, price = current base price, success output shows the price.
- Add `TICKET DELETE;<identifier>`: only tickets dated strictly after the current system date; tickets in a group ticket's composition cannot be deleted individually.
- Add derived user lock: a user is locked while any active ticket (individual or within a group ticket) references them; the user-deletion operation rejects locked users, rejects users in non-modifiable groups and otherwise removes the user from their groups. Historical tickets keep their user reference.
- Extend `HELP` with the ticket commands.
- Commit with the exact message `[F2-C03] Add individual tickets`.

## Capabilities

### New Capabilities
- `tickets`: Ticket abstraction, shared identifier space, individual ticket creation/deletion, and the ticket-derived restriction on user deletion.

### Modified Capabilities
- `execution-control`: HELP additionally lists `TICKET CREATE` and `TICKET DELETE`.

## Impact

- New `Ticket` / `IndividualTicket` model classes, ticket registry and user-deletion operation in `ParkController`, TICKET commands, tests.
- **Flagged ambiguities / ordering conflicts:**
  - The `USER DELETE` command (with password check) belongs to F1-C05, which this batch applies later. This change implements the ticket-based deletion rule in the park controller's user-deletion operation so that F1-C05's command must go through it; there is no `USER DELETE` command yet.
  - Group tickets do not exist yet (F2-C04): the "part of a group ticket" check and the group-lock check are implemented by traversing the (currently empty) group-ticket set.
  - Identifier format for tickets is not specified; only non-empty identifiers are required.
  - Exact output text is not specified; creation prints `TICKET CREATED. PRICE: <price>`.
