# Proposal

## Why

Registered users cannot yet buy anything. This change introduces individual tickets for a date of use, associated with an existing user and priced from a configurable internal price, persisted with history, and makes active tickets protect their owner from deletion. All date checks use the configured system date.

## What Changes

- New ticket: identifier, date of use, reference to the associated registered user, and the price captured at purchase.
- `TICKET CREATE;T001;ana@correo.com;2026-10-15` buys a ticket for an existing user for a date on or after the system date; it answers exactly `OK TICKET CREATED -> <Precio>` (e.g. `OK TICKET CREATED -> 40.00`).
- `TICKET DELETE;T001` deletes a ticket only when its date is strictly later than the system date.
- `SYSTEM PRICE;60.00` changes the internal ticket price; it starts at `40.00` in every execution and is not persisted; existing tickets keep their captured price.
- Tickets persist in a text file and are restored on restart; past tickets are kept as historical records (also when their user was later deleted).
- **Changed Delivery 1 rule:** `USER DELETE` is rejected while the user has any ticket dated on or after the system date; past tickets alone do not block it.
- `HELP` lists the new commands; `uml/model.puml` shows the ticket classes and relationships.
- Out of scope: group tickets and discounts.

The source requests the change name `manage-individual-tickets`; this batch uses the BOTL ID `botl-0006-6-f2-individual-ticket-management`.

## Capabilities

### New Capabilities
- `individual-tickets`: Ticket data, `TICKET CREATE`, `TICKET DELETE` and date rules.
- `ticket-pricing`: The per-execution internal ticket price and `SYSTEM PRICE`.
- `ticket-file-persistence`: Ticket storage, restoration and historical retention.

### Modified Capabilities
- `user-management`: "Authenticated deletion" gains the active-ticket restriction.
- `application-control`: `HELP` lists `TICKET CREATE`, `TICKET DELETE` and `SYSTEM PRICE`.
- `uml-model`: the model must also show the ticket classes and the ticket–user relationship.

## Impact

- New classes `Ticket`, `TicketManager`, `TicketFileStore`; `CommandProcessor` holds the current price and dispatches ticket/price commands and the ticket check on user deletion; `Result` gains verbatim output; `UserManager` gains credential lookup.
- `App` reads/writes `users.txt`, `groups.txt`, `tickets.txt` in a data directory (working directory in production).
- `uml/model.puml` updated.
