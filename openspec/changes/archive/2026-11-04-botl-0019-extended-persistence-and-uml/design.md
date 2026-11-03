# Design

## Context

Repositories (all in the run's data directory, so `--fly` already redirects every file): `users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt` (type column, legacy lines → GENERAL) and `passes.txt` (`Id;Tipo;IdTicket;Precio;Consumido`). `TicketController.delete` / `deleteGroupTicket` remove tickets only; `PassController` keeps passes whose ticket may have been deleted (orphans skipped on reload). See proposal.md.

## Goals / Non-Goals

**Goals:** no orphan passes, atomic cascades across two files, documented rehydration order.

**Non-Goals:** new file formats, transactional storage engine.

## Decisions

- **Rehydration order (unchanged, now documented):** users → groups (members by DNI) → tickets (owner by DNI) and group tickets (group id, members by DNI; constituent tickets rebuilt as `<TG>-<n>` with the discounted share) → attractions (type) → passes (ticket id resolved among individual and constituent tickets). Passes reference tickets by id, tickets reference users by DNI, so every reference points to something loaded earlier.
- **Validation before mutation:** `TicketController.requireDeletable(id, today)` and `requireGroupDeletable(id, today)` perform the existing existence and `date > today` checks without changing state.
- **Cascade order and atomicity (`ParkController`):** individual: check → `passes.removeFor([ticket])` (saves `passes.txt`) → `tickets.delete` (saves `tickets.txt`). Group: check → `passes.removeFor(constituents)` → `tickets.deleteGroupTicket` (removes the group ticket and, with it, its composition). If the ticket step throws, `passes.restore(removed)` re-inserts the passes at their positions and saves again, so the state equals the state before the command. Passes are deleted first as the source requires.
- **UML:** regenerated pieces are verified with a script mapping every model-typed field to one labelled line, plus `plantuml -checkonly`.

## Risks / Trade-offs

- [Crash between the two file writes] → on next start the remaining passes of a still-existing ticket are intact, or passes of a deleted ticket are skipped as orphans; no inconsistent reference survives a reload.
