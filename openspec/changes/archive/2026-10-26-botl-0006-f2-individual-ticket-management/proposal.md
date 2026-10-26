# Proposal

## Why

The park must sell admission. Registered users need individual tickets for a specific date, kept as a history and evaluated against the simulated system date; tickets also change when a user may be deleted.

## What Changes

- New command `TICKET CREATE;Id;Correo;yyyy-MM-dd` buying an individual ticket for a registered user at the current ticket price; response `OK TICKET CREATED -> <Precio>`.
- New command `TICKET DELETE;Id`, allowed only for tickets dated strictly after the system date.
- New command `SYSTEM PRICE;<importe>` changing the current individual ticket price; the price starts at `40.00` on every execution and is not persisted.
- Tickets (identifier, date, holder, price paid) are persisted and restored; past tickets remain as history.
- **Behaviour change**: `USER DELETE` is rejected while the user has a ticket dated on or after the system date.
- New commands appear in `HELP`; the PlantUML model gains the ticket classes.

## Capabilities

### New Capabilities
- `tickets`: Individual ticket purchase, deletion, pricing, history and persistence.

### Modified Capabilities
- `user-management`: user deletion is blocked by current or future tickets.
- `uml-model`: the model must show tickets and their reference to users.

## Impact

- New classes `Ticket`, `TicketRepository`, `TicketManager`; `UserManager` gains deletion checks; `CommandProcessor`, `App` and `uml/model.puml` updated.
- New runtime file `data/tickets.txt`.
