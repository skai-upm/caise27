# Design

## Context

Passes are owned by `IndividualTicket` and stored by `TicketRepository` in `passes.txt`; attraction types are stored by `AttractionRepository`. `TicketController.delete` removes the standalone ticket and `deleteGroupTicket` removes the group ticket and its internal tickets; their passes disappear on the next save because they are part of the removed tickets. `persist()` reloads all tickets from disk when saving fails.

## Goals / Non-Goals

**Goals:**
- Make the deletion order explicit in the code (passes → internal tickets → group ticket) instead of relying on an implicit side effect.
- Prove atomicity under a storage failure.

**Non-Goals:**
- New persistence formats (they already include everything Delivery 3 needs).

## Decisions

- **`IndividualTicket.clearPasses()`** removes all SpeedPasses and the Special Pass.
- **`TicketController.delete`**: validate (exists, standalone, strictly future) → `ticket.clearPasses()` → remove ticket → `persist()`. **`deleteGroupTicket`**: validate → for each internal ticket `clearPasses()` → remove internal tickets → remove group ticket → `persist()`.
- **Atomicity**: every rule is checked before the first change, and `persist()` already reloads tickets, internal tickets and passes from the files when the save fails, discarding the in-memory changes (including cleared passes, since reload creates fresh objects). Tests force a failure by making `tickets.txt` read-only.
- **UML review**: run the scripted cross-check (associations ⇔ fields), PlantUML `-checkonly`, and a Smetana render to inspect the diagram; complete the missing Delivery 3 pieces (e.g. `GroupTicket` → `IndividualTicket` composition label already present; ensure pass composition, capability realisations, generic bindings, `EntryResult`, invoice and help packages are all present) and update the header comment to describe Delivery 3.

## Risks / Trade-offs

- [Reload on failure re-reads every ticket] → Small data sets; correctness over speed.
