# Design

## Context

See proposal.md – Why. Passes: abstract `Pass` (id, `IndividualTicket ticket`, price computed in the constructor, `consumed`), kept by an in-memory `PassRepository` inside `ParkRepositories`. File stores follow the `TextFile` pattern; `ParkRepositories.load(dir)` loads users → groups → attractions → tickets. UML consistency is enforced by `UmlModelTest`.

## Goals / Non-Goals

**Goals:**
- Same persistence mechanism for passes as for other elements; faithful reconstruction (price and state).

**Non-Goals:**
- Diagramming every exception or the command layer in detail.

## Decisions

- **Format `passes.txt`**: `SPEED;id;ticketId;price;consumed` and `SPECIAL;id;ticketId;price;consumed` (`consumed` = `true|false`). Ticket ids may be constituent ids (`TG01:12345678Z`); `;` never appears in identifiers.
- **Reconstruction**: `Pass` gets a protected constructor `(id, ticket, price, consumed)`; `SpeedPass`/`SpecialPass` expose matching `restore(...)` factories used only by the store, so the stored price wins over a recomputation.
- **Ticket lookup** for loading: `TicketRepository.findIndividual(id)` (top-level individual or constituent of a stored group ticket); unknown tickets → record dropped. `TicketService.findIndividual` reuses it.
- **`PassRepository`**: optional `PassFileStore`; `addAll`/`removeAll` persist the snapshot before changing memory; `save()` for in-place consumption. `ParkRepositories.load` builds it after tickets.
- **Consumption persistence**: `ParkController.enterAttraction` calls `PassService.recordConsumption()` (→ `PassRepository.save()`) when passes were consumed.
- **Cascade order**: ticket deletion is persisted first, then its passes are removed; if the pass write failed, the orphaned records are dropped at the next load, so no inconsistent state survives a restart.
- **UML**: add `Pass` hierarchy with `Pass --> "1" IndividualTicket : ticket` and per-type cardinalities (`IndividualTicket "1" <-- "0..*" SpeedPass`, `"0..1" SpecialPass`), invoice classes (`InvoiceBuilder <|.. TextInvoiceBuilder`, `InvoiceService --> InvoiceBuilder : builder`, `Invoice *-- InvoiceSection *-- InvoiceLine`), DI edges (`UserService --> PasswordHasher : hasher`, `PassService --> PassRepository : passes`, `HelpGenerator --> HelpFormatter : formatter`, `InvoiceService --> PassRepository : passes`), `<<singleton>>` on all controllers. `UmlModelTest`: look up fields along the superclass chain, accept multiplicities on the source side, include `business.invoice` and `presentation.help` packages, and count realizations dynamically.
