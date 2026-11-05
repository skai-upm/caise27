# Design

## Context

`Repositories.files(dir)` builds file-backed users/groups/tickets/attractions repositories and an in-memory `PassRepository`. Attraction types are already written as a fourth field.

## Goals / Non-Goals

**Goals:** passes follow the same storage pattern as other elements; final diagram. **Non-Goals:** migrating older files (they simply have no `passes.txt`).

## Decisions

- **`PassStorage` interface / `PassFileStorage`** — format `S|P;identifier;ticketIdentifier;price;consumed(1|0)` (`S` SpeedPass, `P` Special Pass). Loading resolves tickets through the `TicketRepository` (so it runs after tickets); lines whose ticket no longer exists are skipped.
- **`PassRepository(PassStorage, TicketRepository)`**; `save()` writes through the storage after `addAll`, `removeAll` and `update` (consumption). `Repositories.files` passes `passes.txt`; `inMemory` uses `PassStorage.none()`.
- **Diagram**: add packages `business.service` (controllers `<<singleton>>`), `business.invoice` (builder abstraction, concrete builders, `Invoice`, `InvoiceLine`), `presentation` (`ApplicationInitializer`, `CommandProcessor`, `HelpGenerator`/`HelpFormat`), injection arrows `ApplicationInitializer ..> <controller> : injects`, controller → repository associations, and the pass cardinalities expressed on `SpeedPass`/`SpecialPass` → `IndividualTicket`.
- **Consistency test** extended with the Delivery 3 declarations and cardinality lines.

## Risks / Trade-offs

- [Diagram size] → methods hidden; only relevant attributes shown.
