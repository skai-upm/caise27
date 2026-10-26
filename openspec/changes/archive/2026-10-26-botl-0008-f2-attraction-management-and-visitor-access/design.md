# Design

## Context

`Ticket` is abstract with `getUsers()`; `IndividualTicket` returns its user and `GroupTicket` the users of its internal tickets. `TicketManager.findById` searches both kinds. `MainController` holds the system date. Managers follow the in-memory list + repository + persist-with-reload shape.

## Goals / Non-Goals

**Goals:**
- One entry path for every ticket kind, relying on polymorphism (`Ticket.getUsers()`), with no `instanceof` checks.
- Atomic entry: validate everything first, then mutate.

**Non-Goals:**
- Attraction types, passes, deleting attractions, preventing a user from being in two attractions at once (not requested).

## Decisions

- **`Attraction`**: `id`, `name`, `capacity` (final), `List<User> visitors`. `admit(Ticket)` takes `ticket.getUsers()`, rejects if any is already present or `visitors.size() + users.size() > capacity`, then adds all; returns the number admitted. `release(Ticket)` removes the ticket's users who are present and returns how many left. `getOccupancy()`, `getAvailablePlaces()`, `getVisitors()` (unmodifiable).
- **`AttractionManager`** (repository, `TicketManager`, `MainController`): `create`, `enter(attractionId, ticketId)` (checks existence and `ticket.getDate().equals(currentDate)` before delegating to `admit`), `exit(attractionId, ticketId)` (no date check), `findById`. Presence is in memory only.
- **`AttractionRepository`** (`data/attractions.txt`): `id;name;capacity`; malformed lines skipped. Only attraction definitions are saved, so restarting yields empty attractions.
- **Status formatting** in `CommandProcessor`: `OK ATTRACTION STATUS <id>`, then `Name:`, `Capacity:`, `Present:`, `Available:`, `Users present:` and one `- <name> <surname> (<email>)` line per visitor (or `- none`).
- **UML**: `Attraction` (id, name, capacity) with `visitors` association to `User` (`0..*` → `0..*`), `AttractionManager`, `AttractionRepository`, and `Attraction ..> Ticket` usage is omitted (only structural relationships are drawn).

## Risks / Trade-offs

- [A group ticket whose members were later deleted covers fewer users] → Only past tickets can lose members, and past tickets cannot be used to enter.
