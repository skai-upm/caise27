# Design

## Context

`IndividualTicket` stores its own price. `ParkController` holds tickets and acts as invoice director. Persistence uses one repository per element type, composed by `ParkStorage`. See proposal.md - Why.

## Decisions

- **`abstract class Pass`** has `id`, an `IndividualTicket` reference, `consumed`, and an abstract `getPrice()`. `SpeedPass` returns `ticket price × 0.20` (scale 2, HALF_UP) and `SpecialPass` returns the ticket price. Prices are derived from the referenced ticket, which never changes price, so they stay stable and avoid duplicated state. The relationship is one-way (pass → ticket), so tickets don't change.
- **Registry**: `ParkController` keeps a `LinkedHashMap<String, Pass>` (its own identifier space). Operations:
  - `buySpeedPasses(purchaseId, ticketId, quantity)` checks quantity, ticket, date and every `<id>:<n>` before creating anything.
  - `buySpecialPass(id, ticketId)` rejects a ticket that already has a `SpecialPass`.
  - `refundPass(id, Class<? extends Pass>)`.
  - `markPassConsumed(id)`, for the access milestone.
  - `passesOf(ticket)`.
- **Cascades**: `deleteTicket` and `deleteGroupTicket` first validate, then remove the passes of the affected individual tickets, then the tickets. Validation can only fail before any removal, so deletions are never partial.
- **Invoice**: the user-invoice director adds every pass whose ticket belongs to the user (direct or component) through `UserInvoiceBuilder.addPass("<Type> <id> for <ticket>", price)`.
- **Persistence**: `PassRepository` (`passes.txt`) stores `SPEED|SPECIAL;id;ticketId;consumed`. Price isn't stored because it's derived from the persisted ticket price. `ParkStorage` loads it after tickets. `restorePass` keeps the identifier space unique.
- **Commands**: `PassCommands` prints `SPEEDPASS PURCHASED: <n> pass(es). TOTAL PRICE: <x>`, `SPECIALPASS PURCHASED. PRICE: <x>`, `SPEEDPASS REFUNDED` and `SPECIALPASS REFUNDED`.
