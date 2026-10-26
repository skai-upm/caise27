# Design

## Context

Passes are persisted in `passes.txt` (botl-0016), types in `attractions.txt` (botl-0017), consumption saved since botl-0018. `TicketManager.remove` checks the date, removes the top-level ticket from the map and saves; passes disappear implicitly because they are not reachable anymore. If the save throws, the ticket is already gone from memory. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Explicit, ordered cascade; all-or-nothing deletions; tests proving reconstruction and `--fly` isolation; UML matching the code.

**Non-Goals:**
- Atomic multi-file writes at OS level (files are rewritten whole; a crash mid-write is out of scope).

## Decisions

- **Cascade operations in the model**: `IndividualTicket.removePasses()` returns the removed passes (SpeedPasses then Special Pass) and `restorePass` re-attaches them; `GroupTicket.removeComponents()` removes each component's passes, then the components, returning them for undo, with `restoreComponents(...)`.
- **Transactional delete in `TicketManager`**: validate (existence, kind, date) → snapshot map order → cascade removal → `ticketsById.remove` → save; on `RuntimeException` during save, undo in reverse order (restore components/passes, rebuild the map in its original order) and rethrow. The controller turns it into an `ERROR:` response.
- **No format change**: `passes.txt` already stores type, id, ticket, price, status.
- **UML review**: compare `uml/model.puml` with all 49 types via the field cross-check and add anything missing (e.g. `EntryResult`, generics binding).

## Risks / Trade-offs

- [Undo after a partial file write] → the next successful save rewrites all files from memory; only an abrupt crash could leave files inconsistent (out of scope).
