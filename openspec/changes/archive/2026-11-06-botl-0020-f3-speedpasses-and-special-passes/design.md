# Design

## Context

Tickets live in the controller's single ticket map. Composed tickets are `IndividualTicket`s flagged `partOfGroupTicket`. Persistence goes through `ParkState` and the text repositories, built in `AppConfiguration`. `UserInvoiceBuilder` has only identity and direct-ticket steps.

## Goals / Non-Goals

**Goals:** a pass hierarchy that reuses common pass behavior, atomic purchases and cascades, persistence, and invoice sections.
**Non-Goals:** consuming passes at entry (next milestones).

## Decisions

- **Model**: `abstract class Pass` (id, `IndividualTicket ticket`, price, consumed; `consume()`; `isRefundableOn(today)` = !consumed && ticket date after today). Subclasses `SpeedPass` and `SpecialPass` each provide `static BigDecimal priceFor(IndividualTicket)`: 20% (scale 2, HALF_UP) and 100%. The pass refers to its ticket. Tickets do not store passes, and a ticket's passes are found by traversal.
- **Controller**: `Map<String, Pass> passes` (own identifier space).
  - `buySpeedPasses(purchaseId, ticketId, quantity)`: validates quantity ∈ {1, 10}, the ticket (individual, date on or after today) and all n identifiers, then inserts and saves.
  - `buySpecialPass(id, ticketId)`: rejects if any `SpecialPass` refers to the ticket.
  - `refund(passId, Class<? extends Pass> kind)`.
  - Cascades: `deleteTicket` and `deleteGroupTicket` validate first, then remove the passes of the affected tickets, then the tickets, with a single `save()`. Validation happens before any removal, so a rejected deletion changes nothing.
- **Persistence**: `PassRepository` (`passes.txt`), with lines `S;id;ticketId;price;consumed` or `P;...`. On load, ticket ids are resolved against the loaded tickets, including composed ones. Unresolvable lines are warned about and skipped. `ParkState` gains `passes`, `TextParkStorage` gets the repository injected, and `StoragePaths.passes()`.
- **Invoices**: `UserInvoiceBuilder.speedPasses(list)` and `specialPasses(list)` add sections (omitted when empty). `InvoiceController` selects passes whose ticket owner is the user. Group invoices are unchanged.
- **Messages**: `SPEEDPASS PURCHASED. TOTAL PRICE: x`, `SPECIALPASS PURCHASED. PRICE: x`, `SPEEDPASS REFUNDED`, `SPECIALPASS REFUNDED`.

## Risks / Trade-offs

- ["At most one Special Pass throughout its existence" after a refund] → A refunded pass leaves the system, so a new purchase is accepted afterwards. Only an existing pass (consumed or not) blocks. See Open Questions.

## Open Questions

- Whether a refunded Special Pass should still block buying another one for the same ticket. The current interpretation lets it be bought again, because refunded passes leave the persisted state. This can be tightened later without changing the spec's scenarios.
