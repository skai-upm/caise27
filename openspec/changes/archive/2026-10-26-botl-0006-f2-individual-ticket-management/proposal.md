# Proposal

## Why

The park sells admission per day. Registered users must be able to buy an individual ticket for a given date, tickets must survive restarts and remain as history, and a user who still holds a usable ticket must not disappear from the system.

## What Changes

- New command `TICKET CREATE;Id;Correo;yyyy-MM-dd` that sells a ticket for a date to a registered user at the current ticket price and answers `OK TICKET CREATED -> <Precio>`.
- Each ticket has an identifier, date of use, a reference to its user and the price paid.
- Tickets are persisted in their own file and restored at startup; past tickets are kept as history.
- Current ticket price: internal per-execution value starting at `40.00`, changed with `SYSTEM PRICE;60.00`; not persisted (each ticket keeps the price it was sold at).
- New command `TICKET DELETE;Id`, allowed only when the ticket date is strictly after the system date.
- **Changed Delivery 1 rule**: `USER DELETE` is rejected while the user has any ticket dated on or after the system date; past tickets do not block deletion and remain as history.
- All date checks use the system date (`SYSTEM DATE`). HELP and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `ticket-management`: Selling, deleting and persisting individual tickets, and the current ticket price.

### Modified Capabilities
- `user-management`: deletion blocked by current or future tickets.
- `command-processing`: HELP lists TICKET CREATE, TICKET DELETE and SYSTEM PRICE.

## Impact

- New classes `Ticket`, `TicketManager`, `TicketStorage`; `CommandProcessor` routes `TICKET` and `SYSTEM PRICE`, owns the current price, checks tickets before `USER DELETE`; `UserManager.authenticate` becomes public.
- New data file `data/tickets.txt`; `docs/uml/model.puml` updated.
