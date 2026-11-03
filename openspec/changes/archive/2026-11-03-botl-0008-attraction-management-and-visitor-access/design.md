# Design

## Context

Tickets share the abstraction `ParkTicket` (`Ticket`, `GroupTicket` composed of `Ticket`s). `TicketService` owns all tickets; `CommandProcessor` holds the system date and does cross-service checks. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** one access path for both ticket kinds via polymorphism, strict capacity and atomic group entry, a status that does not distinguish how users entered.

**Non-Goals:** attraction types, passes, reservations, deleting or editing attractions (not requested).

## Decisions

- **Polymorphic visitors:** `ParkTicket` gets `abstract List<User> getVisitors()`. `Ticket` returns its owner (empty for history); `GroupTicket` returns the owners of its constituent tickets. `Attraction.enter(ParkTicket ticket, LocalDate today)` only uses `ParkTicket`, so no per-type branching.
- **`Attraction`**: final `id`, `name`, `capacity`, `List<User> present` (identity). `enter` checks `ticket.getDate().equals(today)` (only tickets for the system date), no visitor already present, `present + visitors <= capacity`; all checks happen before any change, so entry is atomic. `exit(Collection<User>)` removes the present ones and returns how many left. `getAvailable() = capacity - present.size()`.
- **`AttractionService`**: holds attractions and `AttractionFileStorage`; create (non-blank unique id, non-blank name, positive integer capacity), find, enter, exit. Saves after creation with revert on failure; entries/exits are not persisted (attractions start empty).
- **Usable tickets:** `TicketService.findUsable(id)` returns an individual or group ticket by id. Constituent tickets (`TG01-1`) are internal parts of a group ticket and are not usable on their own (assumption; the source only shows `T001` and `TG01`).
- **EXIT contract (open question):** the authoritative text uses `ATTRACTION EXIT;A01;T001` (ticket id); the change brief notes an earlier document used an email. Without a decision available in this unattended run, the ticket-id form is implemented as primary and an email of a present user is accepted as a fallback (ids never contain `@`), so both documents' commands work. To be confirmed by the product owner.
- **STATUS output** (plain multi-line text, like HELP):
  ```
  Attraction A01 - Tornado
  Capacity: 20
  Users present: 3
  Available places: 17
  Present: Ana Garcia Lopez <ana@correo.com>, ...
  ```
- **Open question:** whether a user may be present in two attractions at once is not specified; only per-attraction duplicates are prevented.

## Risks / Trade-offs

- [Re-entry with the same ticket after exiting is allowed the same day] → the source does not restrict it.
- [STATUS lists users in entry order] → deterministic and easy to read.
