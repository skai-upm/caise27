# Proposal

## Why

The park needs to sell dated individual tickets to registered users, keep them across executions as historical records, and protect users holding current or future tickets from deletion. All checks must use the configured system date.

## What Changes

- New `Ticket` (identifier, date of use, reference to the owning user, captured price).
- `TICKET CREATE;T001;ana@correo.com;2026-10-15` creates a ticket for a registered user, for a date on or after the system date, at the current price; responds `OK TICKET CREATED -> <Precio>` (e.g. `OK TICKET CREATED -> 40.00`).
- `TICKET DELETE;T001` deletes a ticket only if its date is strictly later than the system date.
- `SYSTEM PRICE;60.00` changes the current ticket price; the price starts at `40.00` on every execution and is not persisted; already created tickets keep their price.
- **Changed Delivery 1 rule:** `USER DELETE` is rejected while the user has any ticket dated on or after the system date; past tickets do not block deletion and are kept as history.
- Tickets persisted in `tickets.txt` and restored at startup; HELP and `uml/model.puml` updated.
- No group tickets or discounts.

## Capabilities

### New Capabilities
- `individual-tickets`: ticket purchase and deletion rules and the TICKET command contract.
- `ticket-pricing`: the execution-wide current ticket price and `SYSTEM PRICE`.
- `ticket-file-persistence`: storage and restoration of tickets, including historical ones.

### Modified Capabilities
- `user-management`: "Authenticated deletion" is also rejected when the user has a ticket dated on or after the system date.
- `application-control`: HELP lists `TICKET CREATE`, `TICKET DELETE`, `SYSTEM PRICE`.
- `domain-model-diagram`: the diagram also represents tickets and their user reference.

## Impact

- New `Ticket`, `TicketService`, `TicketFileStorage`; `CommandProcessor` (price field, new commands, deletion check); `UserService` exposes `authenticate`; `App` wires the ticket service; `uml/model.puml`.
- New runtime file `tickets.txt` (git-ignored). Existing file formats unchanged.
