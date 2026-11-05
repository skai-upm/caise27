# Design

## Context

`Ticket` (abstract) and `IndividualTicket` exist, with a shared registry in `ParkController`. Groups compute their lock through a `GroupTicketDates` provider, which currently has an empty default. See proposal.md - Why.

## Decisions

- **`GroupTicket extends Ticket`**: holds a nullable `Group` reference, the group id, and an unmodifiable `List<IndividualTicket>`. `getPrice()` = sum of component prices, so price behavior is reused rather than duplicated. There is no separate collection of users; components reference users.
- **`IndividualTicket` gains a package-private `containingGroupTicket`**. Components are registered in the shared registry, so the identifier check, the "user has active ticket" traversal and lookups work the same for every type.
- **Atomic creation**: validate everything first (group, non-empty, date, DNIs present, all ids free and distinct), then register. No rollback is needed.
- **Discount**: `GroupTicket.discountFor(int members)` returns a `BigDecimal` rate. Component price = `base × (1 − rate)` at scale 2, rounding `HALF_UP`.
- **Lock source**: the default `GroupTicketDates` provider is now computed from registered group tickets whose group id matches. The setter stays available for tests.
- **Group deletion** traverses group tickets and clears their group reference. The group id string is kept for the historical record.
- **`UserDirectory.dniOf(email)`** is a default method returning `null`. A member without a DNI makes creation fail with an error.
- **User deletion**: `ensureUserDeletable` adds the locked-group check. A new `removeUserFromAllGroups(email)` performs the internal removal once deletion is allowed.

## Risks / Trade-offs

- [Component tickets show up in the global ticket listing] → They are real tickets in the shared identifier space, which the source requires.
