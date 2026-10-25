# Proposal

## Why

The park sells admission. Registered users must be able to buy an individual ticket for a given date, tickets must be kept (including past ones as history), and the existence of current or future tickets must protect their holders from being deleted. All checks must use the simulated system date.

## What Changes

- Individual tickets with identifier, date of use, holder (a reference to the registered user) and price.
- `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` → `OK TICKET CREATED -> <price>`; only for existing users.
- `TICKET DELETE;<id>`: allowed only when the ticket date is strictly later than the system date.
- `SYSTEM PRICE;<price>` changes the current ticket price; the price is an internal value initialised to 40.00 on each execution and not persisted, while each ticket keeps the price it was bought at.
- Tickets persist between executions; past tickets remain as historical records.
- **BREAKING (rule change)**: `USER DELETE` is rejected while the user holds a ticket dated on or after the system date.
- `HELP` lists the new commands; the PlantUML model includes the ticket classes and their relation to users.

## Capabilities

### New Capabilities
- `ticketing`: Individual tickets, their price, creation, refund-deletion, persistence and the date rules that govern them.

### Modified Capabilities
- `user-management`: the "Delete user" requirement adds the rule that users with current or future tickets cannot be deleted.
- `command-processing`: the `HELP` requirement lists `TICKET CREATE`, `TICKET DELETE` and `SYSTEM PRICE`.

## Impact

- New `Ticket`, `TicketManager`, `TicketRepository` (`data/tickets.txt`); `CommandProcessor` (new commands, deletion guard) and `App` wiring; `uml/model.puml`.
- New tests; existing behavior preserved except for the new deletion restriction.
