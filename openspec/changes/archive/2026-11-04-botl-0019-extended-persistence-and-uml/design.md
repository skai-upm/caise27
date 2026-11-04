# Design

## Context

- `TicketManager.delete`/`deleteGroupTicket` validate (`date > today`) and save `tickets.txt` / `group-tickets.txt`.
- `PassManager` saves `passes.txt` and loads passes through `TicketManager.findIndividualTicket`, so it is initialized after tickets in `ParkSession`.
- Constituent tickets are serialized inside their group-ticket line.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- No orphan passes after valid deletions, and no partial state after failures, across two files.

**Non-Goals:**
- Transactions/journaling, deleting consumed-pass history of past tickets (past tickets cannot be deleted).

## Decisions

- **Rehydration order** (already the case, now specified and tested): users → groups (by DNI) → tickets and group tickets (by DNI and group id; constituents rebuilt as `<id>-<k>`) → passes (by individual ticket id, including constituents) → attractions (definitions only).
- **Validation first:** `TicketManager.requireDeletable(id, today)` and `requireGroupDeletable(id, today)` throw the existing errors without mutating, and return the ticket so the controller can collect its individual tickets.
- **Cascade in `ParkController`:**
  1. Validate.
  2. Run `removed = passes.removeFor(individualTickets)`, which saves `passes.txt` without them.
  3. Delete the ticket or group ticket (saves its file). The constituents disappear with the group-ticket line, which satisfies "passes, then constituent tickets, then group ticket".
  4. If step 3 throws, run `passes.restore(removed)` (re-adds them in original order and saves) and rethrow.

  The managers already swap in memory only after a successful save, so a failed step 3 leaves the ticket in memory.
- **`PassManager.restore`** rebuilds the map in original order (by pass id position in the previous map) so files are byte-identical after a rollback.
- **Failure-injection tests** subclass `TicketFileStorage` / `GroupTicketFileStorage` with a `save` that throws, to prove no partial deletion in memory and on disk.
- **UML:** the diagram already includes the Delivery 3 classes, added incrementally with each change. It is reviewed against the code with `UmlModelTest` and the PlantUML syntax check; no speculative elements are added.

## Risks / Trade-offs

- [A crash between the two saves (process killed) could still leave passes removed] → Out of scope without journaling; a failure reported by I/O exceptions is fully compensated.
