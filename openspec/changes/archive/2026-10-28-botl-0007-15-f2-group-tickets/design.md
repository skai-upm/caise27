# Design

## Context

`Ticket` (abstract: identifier, date, active/deletable logic, `getIndividualTickets()`) and `IndividualTicket` (user, price) exist. `ParkController` keeps top-level tickets in a shared map with all-or-nothing `registerTicket` that already checks derived identifiers, `findTicketsOfUser` that traverses compositions, `deleteUser` that checks `isGroupModifiable` (currently always `true`). `User` only has an e-mail.

## Goals / Non-Goals

**Goals:** group tickets with discount, GROUPTICKET CREATE/DELETE, GROUP DELETE, ticket-derived group lock.

**Non-Goals:** full user model/validation (F1-C01/C02), attraction access with group tickets (F2-C07), persistence (F2-C08).

## Decisions

- **Common price behaviour in `Ticket`**: `getPrice()` becomes concrete in `Ticket`, summing the unit prices of `getIndividualTickets()`. `IndividualTicket` holds its unit price (`getUnitPrice()`); for an individual ticket the sum is its own price, for a group ticket it is the total. Alternative: separate price code per subclass — rejected by the source (price behaviour must be reused).
- **`GroupTicket extends Ticket`**: `Group group` (nullable via `detachGroup()`), `List<IndividualTicket> composition` (copied into an unmodifiable list at construction). Static `discountFor(int members)` returns 0/10/20/50 %; `GroupTicket.purchase(id, date, group, basePrice)` builds the composition with ids `id + ":" + dni` and unit price `base × (100 − d) / 100` rounded HALF_UP to 2 decimals.
- **`User` gets `dni`** (constructor `User(dni, email)`), encapsulated; no ticket references.
- **Lock derivation**: `isGroupModifiable(group)` = no top-level `GroupTicket` whose `getGroup() == group` and `isActive(currentDate)`.
- **`deleteGroup`**: rejects unknown/locked groups, removes the group and calls `detachGroup()` on its (historical) tickets.
- **`deleteGroupTicket`**: top-level `GroupTicket` only, `isDeletable(currentDate)`; removing it from the map removes the composition.
- **`deleteIndividualTicket`** already rejects identifiers found only inside compositions.
- **Commands**: `GroupDeleteCommand` (`GROUP DELETED`), `GroupTicketCreateCommand` (`GROUPTICKET CREATED. TOTAL PRICE: <total>`), `GroupTicketDeleteCommand` (`GROUPTICKET DELETED`).

## Risks / Trade-offs

- [Constructor change of `User`] → existing tests updated; F1-C01 will extend the same class with the remaining attributes.
- [Rounding at 50% of odd cents] → HALF_UP per individual ticket; documented in the proposal.
