# Proposal

## Why

The park needs to sell admission. Registered users must be able to buy an individual ticket for a specific date, tickets must be kept (including past ones as history), and the existence of current or future tickets must protect users from being deleted. All date checks must use the simulated system date introduced earlier.

## What Changes

- New command `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` buying an individual ticket for an existing user; on success prints `OK TICKET CREATED -> <price>`.
- Each ticket has an identifier, date of use, a reference to its user, and the price charged.
- New command `SYSTEM PRICE;<price>` changes the current individual ticket price. The price is an internal variable initialised to 40.00 on every execution and is not persisted (the price charged is stored with each ticket).
- New command `TICKET DELETE;<id>`, allowed only when the ticket date is strictly later than the system date.
- Tickets are persisted and restored at startup; past tickets are kept as historical records.
- **Changed user rule**: a user with any ticket dated on or after the system date cannot be deleted. Deleting a user that only has past tickets also removes those tickets, since a ticket must always refer to an existing user.
- HELP lists the new commands; the PlantUML model shows the new classes and relationships.

## Capabilities

### New Capabilities
- `ticket-management`: Buying and cancelling individual tickets, the current ticket price, ticket data and persistence, and how ticket dates are evaluated against the system date.

### Modified Capabilities
- `user-management`: user deletion is now also rejected while the user has current or future tickets, and removes the user's past tickets when it succeeds.

## Impact

- New `Ticket`, `TicketRepository`, `TicketManager`; `MainController` gains the current ticket price; `CommandProcessor` gains TICKET CREATE, TICKET DELETE, SYSTEM PRICE and the deletion check; `App` loads tickets after users.
- New data file `data/tickets.txt`; `uml/model.puml` updated.
- Tests for ticket rules, price, persistence and the user-deletion rule. No new dependencies.
