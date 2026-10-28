# Design

## Context

`IndividualTicket` holds its unit price; `ParkController.findAnyTicket` resolves standalone and composed tickets; deletions check `isDeletable` before removing; `ParkRepositories` bundles four repository ports; `UserInvoiceBuilder` has an empty pass hook; help comes from `CommandCatalog`.

## Decisions

- **Model**: abstract `Pass` (`identifier`, `ticket : IndividualTicket`, `price`, `consumed`; `consume()`, `isRefundable(today)` = `!consumed && ticket.getDate().isAfter(today)`); `SpeedPass` and `SpecialPass` with static `RATE` (20 / 100 %) used by a shared `Pass.priceFor(ticket, rate)` (HALF_UP, 2 decimals). Tickets do not reference passes; passes of a ticket are found by traversal (as with users and tickets).
- **Registry**: `Map<String, Pass> passes` in `ParkController`; `buySpeedPasses(purchaseId, ticketId, quantityText)` validates quantity ∈ {1, 10}, ticket exists and is an `IndividualTicket`, date ≥ current, then checks all ids before inserting; `buySpecialPass(id, ticketId)` checks no existing `SpecialPass` for that ticket; `refundSpeedPass/refundSpecialPass(id)` check type and `isRefundable`.
- **Cascades**: `deleteIndividualTicket` and `deleteGroupTicket` perform all checks first, then remove passes whose ticket is among the deleted individual tickets, then the ticket(s) — nothing mutates before all checks pass.
- **Persistence**: `PassRepository` port (`load(Function<String, Optional<IndividualTicket>>)`, `save`, `getProblems`) added to `ParkRepositories`; `TextPassRepository` writes `passes.txt` lines `S|E;id;ticketId;price;consumed`. Loaded after tickets; passes whose ticket cannot be resolved are reported.
- **Invoices**: `UserInvoiceBuilder` receives the user's passes; `buildPassSections` adds `SpeedPasses` and `Special Pass` sections (omitted when empty). `InvoiceController` collects passes whose ticket's user is the invoiced user (standalone or composed). Group invoices are unchanged (passes excluded).
- **Commands**: `SpeedPassCreateCommand`, `SpeedPassDeleteCommand`, `SpecialPassCreateCommand`, `SpecialPassDeleteCommand`; catalogue entries with descriptions.
- **UML**: add `Pass`, `SpeedPass`, `SpecialPass`, inheritance and `Pass "*" --> "1" IndividualTicket : ticket`, plus `ParkController *--> Pass : passes`.
