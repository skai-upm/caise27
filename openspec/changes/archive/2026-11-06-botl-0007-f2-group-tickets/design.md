# Design

## Context

`Ticket` (abstract) and `IndividualTicket` exist, and all tickets live in one map in `ParkController`. The group lock and user-deletion check currently read group-ticket dates through a stub lookup that returns nothing. Users still do not exist as a model. The controller only knows whether an email is registered, and it now also needs the member's DNI for derived identifiers.

## Goals / Non-Goals

**Goals:**
- Group tickets reuse the common ticket abstraction and price behavior.
- Atomic creation: validate every identifier before inserting anything.
- Group lock and user-deletion checks driven by real stored group tickets.

**Non-Goals:**
- User model or USER DELETE command. Group ticket persistence.

## Decisions

- **Composite**: `GroupTicket extends Ticket` holds an unmodifiable `List<IndividualTicket>` (its composition) and a nullable `Group` reference. `getPrice()` returns the sum of the composed ticket prices, so the total is derived, not stored twice. `Ticket.getPrice()` becomes overridable: the base class stores a price, and `GroupTicket` overrides it with the sum.
- **Reused price behavior**: `Ticket.discounted(BigDecimal base, int percent)` (protected static, scale 2, HALF_UP) is the common rule. `GroupTicket.discountPercent(int members)` holds only the group-specific brackets.
- **Composed tickets** are `IndividualTicket`s with a `partOfGroupTicket` flag. They are stored in the shared ticket map, so the identifier space, `ticketsOf(email)` traversal and the user-deletion check cover them automatically. `TICKET DELETE` rejects flagged tickets.
- **User directory**: `ParkController.UserDirectory` (`Optional<String> dniOf(String email)`, with `exists` defaulting to `dniOf(...).isPresent()`) replaces the existence-only predicate. It defaults to "no users". The user milestones will plug in the real registry.
- **Lock derivation**: `isGroupLocked(id)` scans stored `GroupTicket`s whose group reference is that group (by identity) for a date on or after today. The stub `groupTicketDates` lookup is removed, and tests now create real group tickets.
- **Group deletion** clears the group reference of its remaining (historical) group tickets, which keep their composition and price.
- **Messages**: `GROUP TICKET CREATED. TOTAL PRICE: <total>`, `GROUP TICKET DELETED`.

## Risks / Trade-offs

- [Two members with the same DNI would produce duplicate derived ids] → Derived ids are checked for duplicates within the composition too, and creation is rejected.
- [Historical tests need past tickets, but creation forbids past dates] → Tests create tickets dated today and then advance the system date, which is the real way tickets become historical.
