# Proposal

## Why

Delivery 3 adds personal passes that visitors can buy on top of an individual ticket: SpeedPasses (priority access, bought one at a time or in packs of ten) and a Special Pass (access to special attractions). Each pass is a priced object with its own consumption status that must be stored, refundable while unused under the existing date rules, and kept after use.

## What Changes

- An individual ticket (standalone or internal to a group ticket) may have any number of SpeedPasses and at most one Special Pass.
- New command `SPEEDPASS CREATE;<ticketId>;<1|10>`: creates 1 or 10 independent SpeedPasses, each with its own identifier, price (20% of the price stored in the ticket) and consumption status; prints the amount charged.
- New command `SPECIALPASS CREATE;<ticketId>`: creates the ticket's Special Pass priced at the ticket's stored price; rejected if the ticket already has one, even if it has been used.
- Passes can only be bought for tickets dated on or after the system date.
- New commands `SPEEDPASS DELETE;<passId>` and `SPECIALPASS DELETE;<passId>`: refund an unused pass when the ticket's date permits a refund under the Delivery 2 rule (strictly after the system date); print the amount refunded.
- Consumed passes stay in the system with their status (consumption itself happens at attraction entry in a later change).
- Passes are persisted (identifier, type, ticket, price, consumption status) and restored at startup; they belong to their ticket, so removing a ticket removes its passes.
- User invoices now list the personal passes attached to the user's tickets, including internal tickets of group tickets; group invoices still do not.
- HELP / HELP EXTENDED list the new commands.

## Capabilities

### New Capabilities
- `personal-passes`: SpeedPasses and Special Passes attached to individual tickets — purchase rules and prices, independent pass objects with consumption status, refunds, and persistence.

### Modified Capabilities
- `invoicing`: user invoices list the personal passes attached to the user's tickets.

## Impact

- New model classes `Pass` (abstract), `SpeedPass`, `SpecialPass`; `IndividualTicket` owns its passes; new singleton `PassController`; `TicketRepository` also stores `data/passes.txt`; `TicketController` exposes a save after pass changes; `CommandProcessor` receives its controllers as a `ParkControllers` parameter object (keeps the constructor small) and gains four commands; `InvoiceController` adds a passes section.
- `uml/model.puml` updated. Tests for prices, limits, refunds, persistence and invoices.
