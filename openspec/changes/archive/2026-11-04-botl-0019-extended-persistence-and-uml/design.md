# Design

## Context

`ParkController.deleteTicket` calls `TicketService.delete` (validates, writes `tickets.txt`) and then `PassService.removePassesOf` (writes `passes.txt`); `deleteGroupTicket` does the same after `TicketService.deleteGroupTicket` (writes `tickets.txt` then `grouptickets.txt`, restoring the first on failure). Pass persistence and attraction types already exist.

## Goals / Non-Goals

**Goals:** source-mandated deletion order; no partial cascade on validation or write failures; evidence that restart and `--fly` cover F3 data.

**Non-Goals:** new formats, transactional file system, changes to other commands.

## Decisions

- **Validate first**: `TicketService.checkDeletable(id, today)` and `checkGroupDeletable(id, today)` contain the existing validation (and are reused by `delete`/`deleteGroupTicket`), returning the tickets that will be removed. They change nothing.
- **Order with compensation** in `ParkController`:
  1. validate (no changes);
  2. `List<Pass> removed = passes.removePassesOf(tickets)` (writes `passes.txt`);
  3. delete the ticket(s) via `TicketService` (group: constituents then group ticket, existing two-file save with rollback);
  4. if step 3 throws, `passes.restore(removed)` re-inserts the removed passes in their original positions and saves, then the error is reported.
- **Tests**: failure is simulated by replacing the ticket file with a directory between validation and deletion so the write fails; restart and `--fly` tests exercise passes, consumption and attraction types.
- **UML**: already maintained per change; `UmlModelTest` keeps it consistent; a test asserts the F3 elements listed in the spec are present.

## Risks / Trade-offs

- [Restore can itself fail] → reported as an error; the probability is that of two consecutive write failures on the same directory.
