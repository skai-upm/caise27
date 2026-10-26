# Design

## Context

Passes are owned by `IndividualTicket` (composition) and persisted by `PassController` through `PassRepository` (`passes.txt`); attraction types are already persisted (`Id;Nombre;Capacidad;Tipo`). `TicketController.delete`/`deleteGroupTicket` currently reject tickets with passes (interim rule) and save through `TicketRepository`.

## Goals / Non-Goals

**Goals:** cascade deletion with the required order; all-or-nothing deletions; storage of passes kept consistent; complete UML.

**Non-Goals:** new storage formats (current files already hold the needed data).

## Decisions

- **Cascade in `TicketController`** (owner of tickets, and through composition of their passes):
  - `delete(id)`: validate (exists, standalone, strictly future) → snapshot passes → clear the ticket's passes → remove ticket → persist.
  - `deleteGroupTicket(id)`: validate → snapshot → for each internal ticket clear its passes → remove internal tickets → remove group ticket → persist.
  - Validation happens before any change, so a rejected deletion is a no-op.
- **Rollback on storage failure**: if `repository.save` or a listener throws, the snapshot is restored (tickets re-inserted at their positions, passes re-attached) and the original error is reported.
- **Keeping `passes.txt` consistent**: `TicketController` publishes deletions to listeners registered with `addDeletionListener(Consumer<List<IndividualTicket>>)` (cleared on `configure`). `PassController.configure` registers one that calls `save()`, so the pass file drops the deleted passes. Write order: tickets first, then passes; a pass line whose ticket no longer exists is ignored on load, so even an interrupted write cannot resurrect deleted passes. Alternative: injecting `PassRepository` into `TicketController` — rejected to keep a single owner of pass persistence.
- **`IndividualTicket.clearPasses()`** removes all passes (used by the cascade) and `restorePasses(...)` re-attaches a snapshot.
- **UML**: reconcile with the code using the field cross-check (both directions); listener lists of JDK functional types are not model relationships.

## Risks / Trade-offs

- [Rollback only covers this process's memory; a crash mid-write could leave tickets saved without the pass update] → orphan pass lines are ignored on load, which is the deleted state.
