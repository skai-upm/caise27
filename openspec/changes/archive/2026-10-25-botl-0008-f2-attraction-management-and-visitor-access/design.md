# Design

## Context

`Ticket` is abstract with `getVisitors()`; `IndividualTicket` returns its holder and `GroupTicket` the holders of its composed tickets. `TicketManager.find(id)` returns top-level tickets of either kind (internal `<id>-<n>` tickets are not directly addressable). The `SystemDate` instance is shared. Managers return response strings and persist after successful changes.

## Goals / Non-Goals

**Goals:**
- One access path for both ticket kinds, relying on polymorphism (`Ticket.getVisitors()`), with no `instanceof` on ticket types.
- Atomic capacity checks.

**Non-Goals:**
- Attraction types and passes (Delivery 3).

## Decisions

- **`Attraction`**: `id`, `name`, `capacity` (final), `List<User> present`; `available()`, `canAdmit(List<User>)` (enough places), `admit(List<User>)`, `release(List<User>)` returning how many left, `isEmpty()`.
- **`AttractionManager(AttractionRepository, TicketManager, SystemDate)`**: create/delete/enter/exit/status. `enter` validates attraction → ticket → date (`ticket.getDate().equals(today)`) → nobody of the ticket already inside any attraction → capacity → `admit`. All checks happen before any mutation, which makes group entry atomic.
- **One attraction at a time**: a user already inside any attraction cannot enter again until leaving (`ERROR USER ALREADY INSIDE`); this prevents the same ticket from occupying places twice. Assumption recorded because the proposal does not cover re-entry.
- **Status text** (multi-line, joined with the line separator):
  `ATTRACTION A01 - Tornado` / `Capacity: 20 | Present: 3 | Available: 17` / `Users present:` followed by one line per user `- Ana Garcia Lopez (ana@correo.com)`, or `- none`.
- **Persistence**: `data/attractions.txt` `<id>;<name>;<capacity>`; occupancy only in memory.
- **Consistency with other rules**: users inside an attraction necessarily hold a ticket dated today, so they cannot be deleted and their tickets cannot be refunded; no extra coupling needed.
- **UML**: `Attraction "0..*" o--> "0..*" User : present`, `AttractionManager` associations.

## Risks / Trade-offs

- [`ATTRACTION EXIT` by ticket when only part of a group is inside] → Releases those present; returns how many left.
