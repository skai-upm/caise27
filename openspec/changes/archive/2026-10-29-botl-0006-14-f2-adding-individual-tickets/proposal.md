# Proposal

## Why

Milestone F2-C03 introduces tickets: a common ticket abstraction shared by all ticket types, creation of individual tickets with `TICKET CREATE` at the current system price (no past dates), deletion with `TICKET DELETE` only for tickets dated strictly after the current date, and the rule that a user with a ticket dated on or after the current date cannot be deleted.

## What Changes

- A common ticket abstraction (identifier, date of use, price) reused by every ticket type, with active/deletable state derived from the current system date.
- A single, shared ticket identifier space checked in a common way for all ticket types (including identifiers of tickets nested in other tickets, for later ticket types).
- `TICKET CREATE;<identifier>;<email>;<date>`: creates an individual ticket for an existing user, priced at the current base price, for a date equal to or later than the current system date; displays the price charged.
- `TICKET DELETE;<identifier>`: deletes an individual ticket only if its date is strictly later than the current system date; tickets for today or earlier are kept as historical records.
- The individual ticket references its user (unidirectional; users hold no ticket references). Whether a user has an active ticket is computed by traversing the system's tickets.
- HELP lists both TICKET commands.

## Capabilities

### New Capabilities
- `tickets`: ticket abstraction, shared identifier space, individual ticket creation and deletion, and ticket-derived user state.

### Modified Capabilities
- `command-interface`: HELP also lists `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`.

## Impact

- New classes `Ticket` (abstract), `IndividualTicket`; ticket registry and operations in `ParkController`; dispatch in `CommandProcessor`; tests.

## Ambiguities and unmet prerequisites (flagged)

- **USER DELETE** is a Delivery 1 milestone not yet applied in this batch. The rule "reject deletion if a user has a ticket dated on or after the current system date" is implemented as the derived query `hasActiveTicket(User)` in central management; USER DELETE must use it when it is added. Removal of the user from groups on deletion is part of USER DELETE and is not implemented here.
- **Users** still come only from the programmatic user directory added with groups; through the command interface TICKET CREATE reports the user as not found until USER CREATE exists.
- **Group tickets** (source §7.3) are the next milestone. The abstraction and the identifier-space check are designed to cover nested tickets, but no group ticket type is created here. The rule that individual tickets in a group ticket's composition cannot be deleted with TICKET DELETE therefore has no case to apply to yet.
- The ticket date format is not stated for TICKET CREATE; the `yyyy-MM-dd` format of `SYSTEM DATE` (and of the source example `2027-04-15`) is used, parsed strictly.
- The price display text is not specified; `TICKET CREATED. PRICE: <amount> EUR` is used. Confirmation of deletion: `TICKET DELETED`.
- Identifiers are compared exactly; blank identifiers are rejected.
