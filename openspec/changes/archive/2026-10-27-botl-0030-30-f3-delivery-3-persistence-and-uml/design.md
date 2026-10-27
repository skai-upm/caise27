# Design

## Context

Repositories with file storages exist for users, groups, tickets and attractions (attraction type included since F3-C07). `PassRepository` is memory-only; passes reference their `IndividualTicket`; the model computes prices from the ticket on construction.

## Goals / Non-Goals

**Goals:**
- Persist and restore passes faithfully; final UML.

**Non-Goals:**
- Persisting attraction occupants (still excluded).

## Decisions

- **`passes.txt`** (via `PassFileStorage`), one pass per line: `SPEED|SPECIAL;identifier;ticketIdentifier;price;consumed` (`true`/`false`). Loaded after tickets; the ticket is resolved by identifier in `TicketRepository` (must be an `IndividualTicket`); unresolved or malformed lines are skipped and counted as warnings.
- **Restore with stored price:** `SpeedPass`/`SpecialPass` gain constructors taking an explicit price (used only when loading), so the stored price is kept even if pricing rules or the ticket change; `consume()` restores the state.
- **`PassRepository(PassFileStorage, TicketRepository)`** loads on construction and saves on change and via `persist()`; `ParkRepositories.fromDirectory` creates it; `ParkController.persistAll()` includes it. Consumption (mutation) is written by the per-command `persistAll`.
- **`--fly`**: unchanged mechanism — the factory receives the temporary directory.
- **UML:** add `PassFileStorage`; replace the generic `Pass → IndividualTicket` association by `SpeedPass "0..*" --> "1" IndividualTicket` and `SpecialPass "0..1" --> "1" IndividualTicket` (plus the inherited reference on `Pass`); title "Delivery 3"; `UmlModelTest` gains checks for the listed Delivery 3 elements.
- **Mandatory commit:** `[F3-C09] Extend delivery 3 persistence and UML` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [A pass line whose ticket no longer exists] → Cannot happen through the application (cascade deletion); if found in a hand-edited file it is skipped with a warning.
