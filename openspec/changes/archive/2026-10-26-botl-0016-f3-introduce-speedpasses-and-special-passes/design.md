# Design

## Context

`TicketController` (singleton) owns individual and group tickets and persists them through `TicketRepository` (two files). `CommandProcessor` takes six controllers plus the help map (seven parameters — Sonar's limit). `InvoiceController` builds user invoices from standalone tickets.

## Goals / Non-Goals

**Goals:**
- Passes modelled as part of the individual-ticket aggregate, so later changes (attraction access, cascade deletion) navigate ticket → passes and removing a ticket removes its passes.
- A dedicated singleton controller for pass rules.

**Non-Goals:**
- Using passes at attractions (F3-C08), attraction types (F3-C07).

## Decisions

- **Model**: abstract `Pass` (`id`, `price`, `consumed`; `consume()` throws if already consumed), `SpeedPass` and `SpecialPass`. `IndividualTicket` holds `List<SpeedPass> speedPasses` and `SpecialPass specialPass` (nullable) with `addSpeedPass`, `removeSpeedPass`, `setSpecialPass`, `getPasses()`, `getPass(id)`. Passes do not point back to their ticket (one-way navigability, like other compositions).
- **Prices**: SpeedPass = ticket price × 0.20, scale 2 `HALF_UP` (8.00 for 40.00); Special Pass = ticket price.
- **Identifiers**: `SP<n>` for SpeedPasses and `XP<n>` for Special Passes, `n` = 1 + highest number in use for that prefix (computed from all loaded passes, so unique across restarts).
- **`PassController`** (singleton; `configure(TicketController, MainController)`): `buySpeedPasses(ticketId, quantityText)`, `buySpecialPass(ticketId)`, `refundSpeedPass(passId)`, `refundSpecialPass(passId)`, `findPass(id)`, `ticketOf(pass)`. It looks tickets up through `TicketController.findIndividualTicket(id)` (rejecting group tickets) and persists through `TicketController.save()`. Purchase requires ticket date ≥ system date; refund requires unused pass and ticket date > system date. Refunding a Special Pass removes it, so a new one can be bought afterwards (the "at most one" rule applies to passes held by the ticket, used or not).
- **Persistence**: `TicketRepository(individualFile, groupFile, passFile)`; `data/passes.txt` lines `id;SPEED|SPECIAL;ticketId;price;true|false`, loaded after individual tickets and attached to them; lines naming unknown tickets are skipped. Since passes live inside tickets, every existing removal path (ticket/group-ticket deletion, user/group cascades) drops their passes on the next save.
- **`ParkControllers`** record (presentation) bundling the seven business controllers; `CommandProcessor(ParkControllers, Map<String, HelpGenerator>)`.
- **Invoices**: `InvoiceController.forUser` adds a "Personal passes" section with every pass of every individual ticket of the user (`TicketController.getIndividualTicketsOf(user)`), described as `SpeedPass SP1 (ticket T001)` / `Special Pass XP1 (ticket T001)`.
- **UML**: `Pass` hierarchy; `IndividualTicket *--> SpeedPass : speedPasses` (`0..*`) and `*--> SpecialPass : specialPass` (`0..1`); `PassController` associations; `CommandProcessor x--> ParkControllers : controllers` and `ParkControllers` → each controller.

## Risks / Trade-offs

- [Pass ids depend on the highest existing number] → Refunded passes free nothing in the middle (numbers are not reused unless the highest is refunded); identifiers remain unique among existing passes, which is what lookups need.
