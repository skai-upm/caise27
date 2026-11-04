# Proposal

## Why

Registered users must be able to buy individual park tickets for a given day. Tickets must persist and remain as history, and they change when a user may be deleted. All date checks must use the simulated system date introduced earlier.

## What Changes

- New `Ticket` entity: identifier, date of use, reference to its user, and the price captured at purchase.
- `TICKET CREATE;T001;ana@correo.com;2026-10-15` buys a ticket for an existing user on a date on or after the system date, at the current ticket price. It replies `OK TICKET CREATED -> <Precio>` (for example `OK TICKET CREATED -> 40.00`).
- `TICKET DELETE;T001` deletes a ticket only if its date is later than the system date. Today's and past tickets cannot be deleted.
- `SYSTEM PRICE;60.00` changes the price for later purchases. The price starts at `40.00` in every run and is not persisted. Existing tickets keep their price.
- **Changed rule:** `USER DELETE` is rejected while the user has any ticket dated on or after the system date (`ticketDate >= SYSTEM DATE`). Past tickets alone do not block it and are kept as historical records.
- Tickets are stored in a text file and restored at startup, linked to the existing users.
- `HELP` and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `individual-tickets`: ticket entity, `TICKET CREATE`, `TICKET DELETE`, `SYSTEM PRICE`, history retention.
- `ticket-file-persistence`: ticket file format and restart recovery.

### Modified Capabilities
- `user-management`: "Delete user" gains the active-ticket restriction.
- `application-control`: `HELP` lists `TICKET CREATE`, `TICKET DELETE` and `SYSTEM PRICE`.
- `domain-model-diagram`: the diagram must also show tickets and their relationship to users.

## Impact

- New classes `Ticket`, `TicketManager`, `TicketFileRepository`. `CommandProcessor` holds the ticket price and dispatches the new commands. `UserManager.authenticate` becomes public so the deletion check can run before deletion. `App` wires `tickets.txt`, next to `users.txt`, and it is git-ignored.
- `users.txt` and `groups.txt` formats are unchanged. No new dependencies.
- Out of scope: group tickets and discounts.
