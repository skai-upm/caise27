# Design

## Context

Individual tickets (direct or composed) store their price. Controllers are injected singletons wired by `ParkController.initialize`; repositories come from `ParkRepositories`. Invoices are built with `UserInvoiceBuilder`/`GroupInvoiceBuilder`.

## Goals / Non-Goals

**Goals:**
- Pass model, purchases, refunds, cascade deletion, invoice sections.

**Non-Goals:**
- Consuming passes on entry (F3-C08), persisting passes (F3-C09).

## Decisions

- **Model (`business.model`):** `abstract class Pass` (`identifier`, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`, `consume()`); `SpeedPass` and `SpecialPass` extend it with static price rules (`SpeedPass.priceFor(ticket)` = 20% HALF_UP to cents; `SpecialPass.priceFor(ticket)` = ticket price). Passes reference their ticket; tickets and users do not reference passes (found by traversal).
- **`PassRepository`** (persistence): `LinkedHashMap<String, Pass>` — the pass identifier space; `findByTicket`, `exists`, `save`, `delete`, `findAll`. Added to `ParkRepositories` and injected.
- **`PassService`** (singleton): `buySpeedPasses(purchaseId, ticketId, quantity)` (quantity ∈ {1, 10}, ticket must be an `IndividualTicket` dated ≥ today, all ids checked before saving), `buySpecialPass(id, ticketId)`, `refund(id, Class<? extends Pass> type)`.
- **Special Pass uniqueness (ambiguity, flagged):** a ticket cannot get a second Special Pass while one exists (consumed or not). After a valid refund the pass no longer exists in the system, so a new one may be bought.
- **Cascade deletion:** `TicketService` gets `PassRepository` injected; `deleteTicket`/`deleteGroupTicket` run all checks first, then remove passes, composed tickets and the ticket (no partial deletion).
- **Invoices:** `UserInvoiceBuilder.addSpeedPass` / `addSpecialPass` (sections "SpeedPasses" and "Special Passes"); `InvoiceService` adds the passes of every individual ticket of the user (direct or composed). Group invoices unchanged.
- **Output:** `SPEEDPASS CREATED: SP02:1..SP02:10. TOTAL PRICE: 80.00 EUR`, `SPECIAL PASS CREATED. PRICE: 40.00 EUR`, `SPEEDPASS REFUNDED`, `SPECIAL PASS REFUNDED`.
- **Mandatory commit:** `[F3-C06] Add speed and special passes` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Passes not persisted until F3-C09] → Milestone split defined by the source; F3-C09 extends persistence.
