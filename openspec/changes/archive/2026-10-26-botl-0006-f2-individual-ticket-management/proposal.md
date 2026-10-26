# Proposal

## Why

The park sells admission for specific days. Delivery 2 introduces individual tickets bought by registered users for a date, which also affects whether a user may be deleted.

## What Changes

- `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` buys an individual ticket for a registered user at the current ticket price and prints `OK TICKET CREATED -> <price>`.
- A ticket has an identifier, date of use, associated user (a reference to the user object) and price.
- Tickets are persisted and restored at startup; past tickets are kept as historical records.
- The current ticket price is an in-memory setting initialized to 40.00 on every execution and changed with `SYSTEM PRICE;<amount>`; it is not persisted (each ticket stores the price it was sold at).
- `TICKET DELETE;<id>` deletes a ticket only if its date is strictly after the system date.
- **Changed rule:** `USER DELETE` is rejected while the user has any ticket dated on or after the system date.
- All date checks use `SYSTEM DATE`.
- The PlantUML model is updated with the ticket classes and relationships.

## Capabilities

### New Capabilities
- `ticket-management`: buying, deleting, pricing and persisting individual tickets.

### Modified Capabilities
- `user-management`: user deletion is blocked by current or future tickets.

## Impact

- New `Ticket`, `TicketManager`, `TicketRepository`; `UserManager` deletion split into authenticate + delete; `CommandProcessor`, `App`, `uml/model.puml` updated.
- New data file `data/tickets.txt`.
