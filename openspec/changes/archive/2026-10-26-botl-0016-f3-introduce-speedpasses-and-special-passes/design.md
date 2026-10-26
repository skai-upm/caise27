# Design

## Context

Singleton controllers configured from `App.buildProcessor`; `TicketController` owns all tickets (standalone, internal and group). `CommandProcessor` already takes 7 constructor arguments. The invoice director (`InvoiceController`) builds sections through `InvoiceBuilder`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Pass hierarchy with polymorphic pricing; refunds reuse the ticket date rule.

**Non-Goals:**
- Consuming passes at attractions (C08); cascade deletion (C09).

## Decisions

- **Model** (`business.model`): abstract `Pass` (`id`, `ticket` → `IndividualTicket`, `price`, `consumed`; `consume()`, abstract `getType()`), `SpeedPass` (price = 20% of ticket price, HALF_UP, 2 decimals) and `SpecialPass` (price = ticket price). Price is computed at purchase and stored (the ticket price never changes, but storing keeps restored passes exact).
- **Identifiers**: SpeedPasses `<ticketId>-SP<n>` with `n` = highest existing index for that ticket + 1; Special Pass `<ticketId>-SPECIAL` (one per ticket).
- **`PassController`** (singleton, `configure(PassRepository, TicketController, SystemDate)`): `buySpeedPasses`, `buySpecialPass`, `refundSpeedPass(passId)`, `refundSpecialPass(ticketId)`, `passesOf(IndividualTicket)`, `hasPasses(Ticket)`. Eligibility: `TicketController.find` must return an `IndividualTicket` with `isCurrentOrFuture(today)`. Refund: unused and `ticket.isFuture(today)`.
- **Assumption**: "at most one Special Pass" counts existing passes; after a refund the ticket may buy one again (the refunded pass no longer exists).
- **Deletion guard**: `CommandProcessor` rejects `TICKET DELETE`/`GROUPTICKET DELETE` when `PassController.hasPasses` is true for the ticket or any composed ticket (coordination stays in presentation, as with group locks).
- **Persistence**: `PassRepository` (`passes.txt`): `type;id;ticketId;price;consumed`; loading resolves tickets through a `Function<String, IndividualTicket>` and skips unknown ones.
- **Invoices**: `InvoiceController` gets `PassController` injected and adds section "Personal passes" to user invoices (lines `<passId> <type> (<ticketId>)`).
- **`BusinessControllers` record** (presentation) groups the six controllers so `CommandProcessor(BusinessControllers, SystemDate, HelpGenerator)` stays small. Alternative: an 8-argument constructor — rejected for readability.
- **UML**: `Pass` hierarchy, `Pass x--> "1" IndividualTicket : ticket`, `PassController`, `PassRepository`.

## Risks / Trade-offs

- [Temporary deletion block] → explicit error message; lifted by C09's cascade.
