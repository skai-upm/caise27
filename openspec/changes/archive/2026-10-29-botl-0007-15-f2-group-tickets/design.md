# Design

## Context

`Ticket` (abstract: identifier, date, price, `identifiers()`, `involves`) and `IndividualTicket` exist; `ParkController` keeps top-level tickets and a stub `hasActiveGroupTicket` returning false. `User` has only an email.

## Goals / Non-Goals

**Goals:** `GroupTicket` composed of `IndividualTicket`s; discount; effective group lock; deletion rules; user-deletion check.

**Non-Goals:** USER DELETE command, DNI validation, persistence.

## Decisions

- **`User(email, dni)`**: private final `dni` + getter; equality stays by email. Tests updated to supply DNIs.
- **`GroupTicket extends Ticket`**: private final `List<IndividualTicket> composition` (unmodifiable copy) and a `Group group` reference that can be cleared (`releaseGroup()`) when the group is deleted. Its price passed to `super` is the sum of the composition's prices, so the common price behavior is reused; `identifiers()` = own id + composition ids; `involves(u)` = any composed ticket involves `u`. No user collection is stored.
- **Discount**: `GroupTicket.discountPercent(int members)` with the four brackets; `GroupTicket.discountedPrice(base, members)` = `base × (100 − pct) / 100`, scale 2, `HALF_UP`. Creation in `ParkController.createGroupTicket`: validate group (exists, non-empty), date ≥ today, build all component tickets, then check every identifier (group id + derived ids, also against each other) before registering — no partial creation.
- **Registry**: only top-level tickets (individual and group) are in the map; composed tickets are reachable through their group ticket, so TICKET DELETE finds a composed id via traversal and rejects it explicitly ("belongs to group ticket ...").
- **Lock**: `hasActiveGroupTicket(g)` = any `GroupTicket` with `getGroup() == g` and `isActiveOn(currentDate)`; computed every time.
- **GROUP DELETE**: if allowed, call `releaseGroup()` on its (historical) group tickets, then remove the group.
- **`canDeleteUser(User)`**: `!hasActiveTicket(user)` and no group containing the user `hasActiveGroupTicket`.

## Risks / Trade-offs

- [Equality by email while derived ids use DNI] → duplicates in DNI surface as identifier conflicts (flagged in proposal).
