# Proposal

## Why

Registered users can now be managed and grouped, but they cannot buy anything. This change introduces individual tickets for a date of use, a configurable ticket price, ticket persistence with historical retention, and the new rule that users holding a current or future ticket cannot be deleted — all evaluated against the configured `SYSTEM DATE`.

## What Changes

- New `Ticket` with identifier, date of use, a reference to its `User`, and the price captured at purchase.
- New command `TICKET CREATE;T001;ana@correo.com;2026-10-15`: only for registered users, unique identifier, date of use not before the system date; answers exactly `OK TICKET CREATED -> <Precio>` (e.g. `OK TICKET CREATED -> 40.00`).
- New command `TICKET DELETE;T001`: allowed only when the ticket date is strictly later than the system date.
- New command `SYSTEM PRICE;60.00`: changes the current individual ticket price; the price lives in the main controller, starts at `40.00` on every execution and is not persisted. Existing tickets keep their captured price.
- New `tickets.txt` file; tickets are restored on startup and past tickets are never removed automatically (historical records).
- **Modified rule**: `USER DELETE` is rejected while the user has any ticket dated on or after the system date; past tickets alone do not block it.
- `HELP` lists the three new commands; `uml/model.puml` gains `Ticket`, `TicketService`, `TicketRepository` and their relationships.

## Capabilities

### New Capabilities
- `individual-tickets`: Ticket data, `TICKET CREATE` and `TICKET DELETE` rules, and the effect of tickets on user deletion checks.
- `ticket-pricing`: The execution-wide current ticket price and the `SYSTEM PRICE` command.
- `ticket-file-persistence`: Ticket file format, saving and restoring, and retention of historical tickets.

### Modified Capabilities
- `user-management`: "Delete user" gains the current/future-ticket restriction; "Command format and responses" allows success responses whose text is prescribed by a command's source (`OK TICKET CREATED -> <Precio>`).

## Impact

- **Code**: new `Ticket`, `TicketRepository`, `TicketService`; `CommandProcessor` (price field, three commands, deletion check, HELP), `UserService.authenticate` made public, `App` wiring of `tickets.txt`.
- **Data**: new `tickets.txt` (`Identificador;DNI;yyyy-MM-dd;Precio`); existing files unchanged.
- **Docs**: `uml/model.puml` updated.
- **Out of scope**: group tickets, discounts.
- **Open questions / assumptions**:
  - Creating a ticket dated before the system date is rejected (acceptance scenario 2 of the brief; the source only says "for a particular date").
  - `SYSTEM PRICE` accepts a positive amount written with digits and an optional `.` followed by one or two decimals; prices are shown with two decimals.
  - When a user with only past tickets is deleted, their tickets are retained as history and still identify the holder by DNI; after a restart such a ticket has no registered user to reference.
  - Ticket identifiers are case-sensitive, non-blank and unique across all tickets (including historical ones).
  - Response texts other than `OK TICKET CREATED -> <Precio>` are not prescribed (`OK: Ticket deleted`, `OK: Ticket price set to 60.00`).
