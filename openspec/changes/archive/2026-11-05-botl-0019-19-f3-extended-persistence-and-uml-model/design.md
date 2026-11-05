# Design

## Context

`TicketManager.delete(id, today)` and `deleteGroupTicket(id, today)` validate and save their own files; `PassManager` saves `passes.txt`; `ParkController` coordinates cross-entity rules. Passes reference `IndividualTicket` objects (standalone or constituents). Pass and attraction-type persistence already exist. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** cascade in the required order with no partial result; explicit restart and `--fly` coverage; UML completeness.

**Non-Goals:** a transactional storage engine.

## Decisions

- **Validation first**: `TicketManager.checkDeletable(id, today)` and `checkGroupDeletable(id, today)` return the rejection (same messages as today) or `null`; nothing is touched when they fail.
- **Order and compensation in `ParkController`**: (1) `PassManager.removePassesOf(tickets)` saves `passes.txt` without those passes and returns the removed passes; (2) `TicketManager.delete`/`deleteGroupTicket` (which removes constituents together with the group ticket in one save of `grouptickets.txt`); (3) if step 2 fails, `PassManager.restore(removed)` saves the passes back and the error is returned. Step 1 failing leaves everything untouched (save-then-swap). This realizes "passes first, then constituent tickets, then group ticket" with compensation.
- **Output**: existing message plus ` N pass(es) deleted.` when N > 0 (no change when the ticket had no passes, keeping legacy output identical).
- **Restart and fly**: covered by tests that restart through `ApplicationContext` and by an `--fly` run comparing normal files byte for byte.
- **UML**: verified with the cross-check script; any missing F3 element is added.

## Risks / Trade-offs

- [Compensation write could also fail] → reported as an error; the in-memory state is restored regardless, and the next successful save rewrites the file consistently.
