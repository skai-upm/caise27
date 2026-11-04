# Proposal

## Why

Registered users must be able to buy individual tickets for a specific date. Tickets are the first date-dependent business object: they use the simulated `SYSTEM DATE`, must be kept as history, and change when a user may be deleted.

## What Changes

- New `Ticket` entity: identifier, date of use, reference to the owning `User`, and the price captured at purchase.
- New command `TICKET CREATE;Id;Correo;yyyy-MM-dd`, which needs an existing user, a unique identifier and a date on or after the system date. On success it returns exactly `OK TICKET CREATED -> <Precio>` (e.g. `OK TICKET CREATED -> 40.00`).
- New command `TICKET DELETE;Id`, allowed only when the ticket date is strictly later than the system date.
- New command `SYSTEM PRICE;60.00` to change the current ticket price. The price is held in memory, starts at `40.00` on each execution and is not persisted. Existing tickets keep their captured price.
- **Changed rule (Delivery 1):** `USER DELETE` is rejected while the user owns any ticket dated on or after the system date. Past tickets do not block deletion and are retained as history.
- Tickets persist in `tickets.txt` and are restored at startup, linked to the loaded users.
- `HELP` and the PlantUML model are updated.

## Capabilities

### New Capabilities
- `individual-tickets`: Ticket entity, `TICKET CREATE`/`TICKET DELETE` rules and historical retention.
- `ticket-price`: The execution-wide current ticket price and `SYSTEM PRICE`.
- `ticket-file-persistence`: Text-file storage and restoration of tickets.

### Modified Capabilities
- `user-management`: "Authenticated deletion" is blocked by current or future tickets.
- `application-control`: "HELP command" lists the ticket and price commands.
- `uml-model`: "PlantUML class model" includes tickets and their owner relationship.

## Impact

- New: `Ticket`, `TicketManager`, `TicketFileStorage` and tests.
- Changed: `CommandProcessor` (price, ticket commands, deletion check), `UserManager` (public `authenticate`), `App` (wires `tickets.txt`), `docs/uml/model.puml`.
- Group-ticket purchases and discounts are not included.
