# Design

## Context

Business rules sit in singleton controllers configured by `ApplicationInitializer`; tickets are in `TicketRepository` (group components inside their `GroupTicket`).

## Decisions

- **Model**: `abstract class Pass` (`id`, `IndividualTicket ticket`, `BigDecimal price`, `boolean consumed`, `consume()`, `isRefundable(today)` = `!consumed && ticket.getDate().isAfter(today)`); `SpeedPass` (price = ticket price × 0.20, half-up) and `SpecialPass` (price = ticket price). Price computed once at creation from the ticket's stored price.
- **`PassRepository`** (persistence.repository, in memory for now): `LinkedHashMap<String, Pass>`, `exists`, `add`, `addAll`, `remove`, `findById`, `findByTicket(IndividualTicket)`, `findAll`.
- **`TicketRepository.findIndividualTicket(id)`**: a top-level `IndividualTicket` or a group-ticket component.
- **`PassController`** singleton, `configure(PassRepository, TicketRepository, ParkController)`: `buySpeedPasses`, `buySpecialPass`, `refundSpeedPass`, `refundSpecialPass`, `removePassesOf(Collection<IndividualTicket>)`. Atomic package creation: compute all ids, check, then add.
- **Cascade**: `TicketController` (now configured with `PassController`) calls `removePassesOf` before removing the ticket(s); all checks happen before any removal, so no partial deletion.
- **UML**: the pass hierarchy is added to `uml/model.puml` so the diagram–code consistency test keeps passing; the full Delivery 3 diagram revision is F3-C09.
- **Invoices**: `InvoiceController` also receives `PassRepository`; user invoice adds sections `SpeedPasses` and `Special passes` for passes whose ticket's user is the invoiced user.
