# Design

## Context

Tickets form a hierarchy: abstract `Ticket` with `IndividualTicket` (one user) and `GroupTicket` (composed of individual tickets). `TicketManager.find(id)` resolves any ticket id. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**
- One entry/exit algorithm for every ticket type, via polymorphism.
- Atomic group entry.

**Non-Goals:**
- Attraction types, passes (Delivery 3); persisting occupancy.

## Decisions

- **Polymorphic holders**: abstract `Ticket.getHolders()` — `IndividualTicket` returns its user, `GroupTicket` returns the users of its composed tickets. `Attraction` only depends on `Ticket`. Alternative: `instanceof` branches in the attraction — rejected (duplication, the epic requires OO handling).
- **`Attraction`**: `id`, `name`, `capacity`, `Set<User> visitors` (`LinkedHashSet`, identity of registered users). `enter(Ticket, LocalDate today)` validates date, duplicates and capacity for the whole holder list before adding any; `exit(Ticket)` removes the holders present; `available()` = capacity − visitors.
- **`AttractionManager(AttractionRepository, TicketManager, SystemDate)`**: create/delete/enter/exit/status; resolves tickets via `TicketManager.find`. Status text is built by `CommandProcessor` from the attraction's getters.
- **Persistence**: `data/attractions.txt` lines `id;name;capacity`; visitors are not stored, so attractions start empty.
- **Output**: `OK ATTRACTION STATUS <id>` followed by lines `Name`, `Capacity`, `Present`, `Available`, `Users` (one per line, `- Name Surname (DNI)`, or `- none`).
- **UML**: `Attraction`, `AttractionManager`, `AttractionRepository`; `Attraction o--> "0..*" User : visitors`.

## Risks / Trade-offs

- [A user can be in several attractions at once] → not forbidden by the epic; only duplicates within one attraction are rejected.
