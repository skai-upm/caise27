# Design

## Context

Tickets live in `ParkController` (top-level map; components inside `GroupTicket`). Repositories are injected through the `Repositories` record. Consumption is a later milestone.

## Goals / Non-Goals

**Goals:** pass model, purchase, refunds, cascade, persistence. **Non-Goals:** consumption via attraction entry; invoice pass sections.

## Decisions

- **`abstract class Pass`** (business.model): private final `identifier`, `price`, `IndividualTicket ticket`; private `consumed`; `isConsumed()`, `consume()`; `isRefundableOn(today)` = `!consumed && ticket.getDate().isAfter(today)`. **`SpeedPass`** with static `unitPrice(ticket)` = 20 % (scale 2, HALF_UP); **`SpecialPass`** with `priceFor(ticket)` = ticket price. Tickets and users hold no pass references (passes know their ticket).
- **Registry**: `LinkedHashMap<String, Pass> passes` in `ParkController`; `findIndividualTicket(id)` searches top-level individual tickets and group-ticket components. `buySpeedPasses(purchaseId, ticketId, quantityText)`: quantity "1"/"10"; ticket exists and date ≥ today; build ids, check all free, then add. `buySpecialPass(id, ticketId)`: plus no existing Special Pass for that ticket. `refundSpeedPass` / `refundSpecialPass`: type check, `isRefundableOn`.
- **Cascade**: `deleteTicket` / `deleteGroupTicket` run their existing checks first, then remove passes whose ticket is the deleted ticket (or any component), then the ticket — checks precede mutations, so no partial deletion.
- **Persistence**: `PassRepository` interface; `Repositories` gains `passes`; `TextFilePassRepository` lines `SPEED|SPECIAL;id;ticketId;price;consumed`, loaded after tickets via `ParkController.restorePass`.
- **Presentation**: four new dispatch cases and catalog entries.

## Risks / Trade-offs

- [`Repositories` record changes arity] → the factory and tests are updated together.
