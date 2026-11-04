# Design

## Context

- Files: `users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt` (`Id;Nombre;Capacidad;Tipo`, legacy 3-field lines → GENERAL), `passes.txt` (`SPEED|SPECIAL;id;ticketId;price;consumed`). All are siblings of the users file, so `--fly` already redirects them.
- Rehydration order at startup (in `App`): users → groups (by DNI) → tickets and group tickets (users by DNI, groups by id; constituents rebuilt from DNIs with the discounted member price) → attractions (definitions, empty) → passes (linked to individual tickets by id via `TicketManager.findIndividualTicket`, keeping stored price and consumed flag).
- `ParkController.deleteTicket`/`deleteGroupTicket` currently reject tickets that have passes.

## Goals / Non-Goals

**Goals:** cascading deletions in the source's order with no partial result; evidence that F3 state round-trips.

**Non-Goals:** file-format changes; transactional files (a compensation approach is enough for this single-user application).

## Decisions

- **Validation first.** `TicketManager.checkDeletable(id, today)` and `checkGroupDeletable(id, today)` perform every existing deletion check (exists, right kind, date strictly after today) and return the ticket, without mutating; `delete`/`deleteGroupTicket` reuse them.
- **Cascade sequence (controller).**
  1. Check deletable (no change on failure).
  2. Collect passes: of the ticket, or of every part of the group ticket.
  3. `passes.removeAll(list)` — removes and saves passes.txt (own rollback if that save fails).
  4. Delete the ticket: individual → `tickets.delete`; group → `tickets.deleteGroupTicket` (constituents live inside the group ticket, so removing the group ticket removes them in the same save, after their passes, matching "passes, then individual tickets, then the group ticket").
  5. If step 4 throws, `passes.restore(list)` re-adds the removed passes (original order) and saves again, then the error is reported.
- **Order inside `PassManager.restore`.** Passes are re-inserted preserving the global insertion order by rebuilding the map from a snapshot taken before removal.
- **Tests.** Restart round-trips for consumed passes and typed attractions; cascade tests; failure tests (invalid date → nothing removed; ticket file unwritable → passes restored, using a ticket repository whose file path is replaced by a directory after setup); `--fly` test extended to passes.
- **UML.** Verified against the code with the field/label checker and `UmlModelTest`; no element is added beyond implemented code.

## Risks / Trade-offs

- [Two files are written separately] → compensation restores passes if the ticket save fails; a crash between the two writes could still leave passes deleted with the ticket present, which is acceptable here and documented.
