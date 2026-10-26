# Design

## Context

`IndividualTicket` (id, date, user, price) lives in `TicketController` (Singleton, configured in `App.initialize`). Repositories are business interfaces with file implementations. Invoices are assembled by `InvoiceDirector` on a builder. Attraction access (where passes get consumed) is a later change.

## Goals / Non-Goals

**Goals:** pass model and rules, refunds, persistence, invoice section.

**Non-Goals:** consuming passes at attractions and cascade deletion of passes (later changes); pack discounts.

## Decisions

- **Model** (`business.model`): abstract `Pass` (`id`, `price`, `consumed`; `consume()`), `SpeedPass` and `SpecialPass`. `IndividualTicket` owns its passes (composition): `List<SpeedPass> speedPasses` and `SpecialPass specialPass` (nullable), with `addSpeedPass`, `setSpecialPass`, `removePass`, `getPasses()`. Passes have no back-reference; lookups by id scan the tickets. Alternative: passes referencing tickets — rejected to keep navigability one-way (ticket → passes) as stated ("a ticket may have passes").
- **Prices**: SpeedPass = ticket price × 0.20 (scale 2, HALF_UP); Special Pass = ticket price.
- **Identifiers**: SpeedPass `<ticketId>-SP<n>` with `n` = highest existing number for that ticket + 1; Special Pass `<ticketId>-SPECIAL`.
- **Special Pass uniqueness**: the ticket refuses a second Special Pass while one exists (used or not). Assumption: a refunded Special Pass no longer exists, so it can be bought again.
- **`PassController`** (Singleton, `configure(PassRepository, TicketController, SystemDate)`): `buySpeedPasses(ticketId, quantity)`, `buySpecialPass(ticketId)`, `refundSpeedPass(id)`, `refundSpecialPass(id)`; ticket must be an `IndividualTicket` dated ≥ system date for purchases; refunds need ticket date > system date and unused pass. It loads passes in `configure` by attaching them to tickets obtained from `TicketController.findById`, and saves after each change.
- **Persistence**: `PassRepository` interface (`load(Function<String, IndividualTicket>)`, `save(List<IndividualTicket>)`); `FilePassRepository` writes `passes.txt` lines `SPEED|SPECIAL;passId;ticketId;price;consumed`. `App.initialize` creates it in the data directory, so `--fly` applies.
- **Interim deletion rule**: `TicketController.delete`/`deleteGroupTicket` reject tickets with passes (`IndividualTicket.hasPasses()`), until cascade deletion is specified.
- **Invoice**: `TicketController.individualTicketsOf(User)` (direct and internal); `InvoiceDirector.constructUserInvoice` receives that list too and adds a "Passes" section (one line per pass: `T001-SP1 SpeedPass`, price) only when passes exist.
- **Commands** added to `CommandProcessor` and `CommandCatalog`; responses: `OK SPEEDPASS CREATED -> 10 x 8.00 = 80.00 (T001-SP1 .. T001-SP10)`, `OK SPECIALPASS CREATED -> T001-SPECIAL -> 40.00`, `OK SPEEDPASS REFUNDED -> T001-SP1 -> 8.00`, `OK SPECIALPASS REFUNDED -> T001-SPECIAL -> 40.00`.

## Risks / Trade-offs

- [Ticket deletion blocked by passes is stricter than the final rule] → replaced by cascade deletion in the persistence change (F3-C09).
