# Design

## Context

`Park` holds users, groups, date and price. Group tickets (F2-C04) will be composed of individual tickets, one per member, with identifiers `<groupTicket>:<dni>`, and a discount.

## Goals / Non-Goals

**Goals:** an abstraction that group tickets can extend without duplicating price logic; one identifier check for all types. **Non-Goals:** group tickets, persistence.

## Decisions

- **`abstract class Ticket`** holding `identifier`, `date`, and shared behavior: `getPrice()` (abstract), `isActiveOn(date)` (date ≥ today), `isDeletableOn(date)` (date > today), `formatPrice()`. **`IndividualTicket extends Ticket`** with its `User` and its own unit price (base price at purchase). F2-C04's `GroupTicket extends Ticket` will compute its price from its composition. An abstract class (rather than only an interface) is chosen because identifier/date fields and lifecycle rules are shared state, not just a contract.
- **`TicketRepository`** stores every ticket (including future composition tickets) keyed by identifier; `ensureAvailable(Collection<String>)` is the single uniqueness check, usable for a group ticket's derived identifiers before anything is stored (no partial creation).
- **`TicketService`** uses `Park` for current date and price; `ticketsOf(User)` traverses the repository (users hold no ticket references).
- **`UserService.deleteUser`** asks the ticket repository for active individual tickets of the user before removing; historical tickets keep referencing the `User` object, so their data remains available after deletion.
- Response for creation: `TICKET CREATED. Price: 40.00 EUR` (price with two decimals).

## Risks / Trade-offs

- [Deleted users still referenced by historical tickets] → intended (fiscal records); the user is no longer in the user repository.
