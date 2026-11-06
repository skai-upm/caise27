# Design

## Context

See proposal.md – Why. `ParkController` (single instance) holds the current date and base price and the user/group services; groups are locked via a predicate that is currently always false. `UserService.update` currently swaps the stored `User` for a modified copy.

## Goals / Non-Goals

**Goals:**
- Common ticket behavior written once; concrete types add only what differs.
- One place that checks identifier uniqueness for every ticket type (group-ticket compositions in F2-C04 plug into the same check).
- Cross-entity rules (user deletion vs tickets and groups) coordinated by the central `ParkController`.

**Non-Goals:**
- Group tickets and persistence of tickets.

## Decisions

- **`Ticket` (abstract, `es.upm.park.model`)**: final `id`, `date`, `price` (`BigDecimal`), `isActive(today)` = date ≥ today, `isDeletable(today)` = date > today, and abstract `involves(User)` so callers can ask whether a ticket concerns a user without knowing the type. **`IndividualTicket extends Ticket`** with a `User` reference. Abstract class (not interface) because state and price behavior are shared.
- **`TicketRepository`**: `existsId(id)` is the single uniqueness check, implemented over `allIds()` which each ticket contributes to (`Ticket.ids()`, default = own id; group tickets will add composition ids). `findById`, `add`, `remove`, `findAll`.
- **`TicketService(TicketRepository, UserRepository, Supplier<LocalDate>, Supplier<BigDecimal>)`**: `createIndividual`, `delete`, `hasActiveTicket(User)` traversing tickets (users hold no ticket references).
- **User deletion orchestration in `ParkController.deleteUser(email, password)`**: authenticate (`UserService.authenticate`), reject if `ticketService.hasActiveTicket(user)`, reject if any group containing the user is locked, then remove the user from all groups and `UserService.remove(user)` (persisted). Alternative: give `UserService` references to tickets and groups — rejected: it would make the user service depend on every other element.
- **In-place user update**: `UserRepository.update(user, change)` applies `change` to `user.copy()`, persists the snapshot with the copy, then applies `change` to the stored object. Tickets therefore keep pointing at an always-current user, and a failed write still changes nothing.
- **Dates** parsed with the same strict `yyyy-MM-dd` parser as `SYSTEM DATE` (moved to a small `Dates` helper in the controller package).
- **Messages**: `Ticket created: T001 price 45.50`, `Ticket deleted: T001`; errors `ticket T001 already exists`, `ticket date 2027-04-14 is before the current date 2027-04-15`, `no ticket T001`, `ticket T001 cannot be deleted: it is not dated after 2027-04-15`, `user ana@correo.com has an active ticket`, `user ana@correo.com belongs to group G01 which has an active group ticket`.

## Risks / Trade-offs

- [Historical tickets reference users that were deleted] → intended (fiscal record); the ticket keeps the `User` object with its data. Persisting it is handled in F2-C08.
