# Design

## Context

Individual tickets store their price; composition tickets are `IndividualTicket`s held by a `GroupTicket`. Controllers are singletons with injected repositories; invoices are built by builders with optional steps.

## Goals / Non-Goals

**Goals:** pass model reusable by attraction entry (F3-C08) and persistence (F3-C09). **Non-Goals:** consumption at entry, pass files.

## Decisions

- **`abstract class Pass`** (`identifier`, `IndividualTicket ticket`, `price` fixed at purchase, `consumed`, `consume()`, `isRefundableOn(today)`); **`SpeedPass`** (`RATE = 0.20`) and **`SpecialPass`** compute their price from the ticket at construction. A pass knows its ticket; tickets do not know passes (found by traversal, like other relationships).
- **`PassRepository`**: single map for all pass types (shared pass identifier space), `ensureAvailable(ids)`, `passesOf(ticket)`, `addAll/removeAll`. Part of `Repositories` (in-memory for now, persistence in F3-C09) and exposed by `Park`.
- **`PassService`** singleton (injected `TicketRepository`, `PassRepository`): purchase/refund rules; quantity accepted only as the literal `1` or `10`.
- **Assumption (source ambiguous)**: "at most one Special Pass" is enforced while a Special Pass for the ticket exists in the system (consumed ones included); after a valid refund the purchase no longer exists, so a new one is allowed.
- **Cascade**: `TicketService` receives the `PassRepository` (injected collaboration); it checks deletability first and then removes passes, composition tickets and the group ticket, so a rejected deletion removes nothing.
- **Invoices**: `UserInvoiceBuilder.speedPasses(...)` and `.specialPasses(...)` steps; `InvoiceService` passes the user's passes (via all of the user's individual tickets).
- Responses: `SPEEDPASS CREATED: SP02:1..SP02:10. Total price: 80.00 EUR`, `SPECIALPASS CREATED. Price: 40.00 EUR`, `SPEEDPASS DELETED`, `SPECIALPASS DELETED`.

## Risks / Trade-offs

- [Passes are lost at exit until F3-C09] → expected per roadmap.
