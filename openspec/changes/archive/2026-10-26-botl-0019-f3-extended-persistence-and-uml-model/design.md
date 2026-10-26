# Design

## Context

`TicketManager.delete` removes a standalone ticket from its map and saves tickets and group tickets; `deleteGroupTicket` removes the group ticket. Passes are composed inside `IndividualTicket` and persisted only by `PassManager` (`TextFilePassRepository`, loaded after tickets). `PassManager` depends on `TicketManager`, so `TicketManager` cannot call `PassManager` without a cycle.

## Goals / Non-Goals

**Goals:**
- Ordered, explicit cascade and all-or-nothing refunds, persisted consistently in all files.

**Non-Goals:**
- Transactions across processes or crash-safe file writes beyond restoring the previous content on failure.

## Decisions

- **Pass storage visible to `TicketManager`**: `TicketManager.initialize` additionally receives the `PassRepository` (same instance `PassManager` uses, created once in `Bootstrap`). Its `persist` writes passes, then tickets, then group tickets, so a refund rewrites all three. `PassManager` keeps loading passes and handling pass operations.
- **Ordered cascade**: `IndividualTicket.clearPasses()` returns the removed passes; refunds call it first (for every composed ticket of a group ticket), then remove the individual tickets/group ticket from the maps.
- **All-or-nothing**: validation (existence, refundability) happens before any mutation. If persisting fails, the manager restores the removed passes (`restorePasses`) and map entries and rewrites the files from the restored state (best effort), then answers `ERROR STORAGE FAILURE`.
- **Fly mode** already routes all repositories through the data directory; tests confirm passes and attraction types land in the temporary directory.
- **UML**: add `TicketManager "1" x--> "1" PassRepository : passRepository`; `UmlModelTest` keeps the model honest.

## Risks / Trade-offs

- [Two managers write `passes.txt`] → Both write the complete set from the same in-memory tickets, so the last write is always consistent.
