## Context

- Tickets live in `TicketRepository`. `IndividualTicket` has a stored `price`; group components have identifiers `<group>:<dni>` and a discounted price.
- `TicketManager.deleteIndividual` and `deleteGroupTicket` enforce the Delivery 2 rules.
- Repositories are injected through `ParkRepositories`, and managers are injected singletons (F3-C03).
- `InvoiceBuilder.buildPersonalPasses()` is the hook that F3-C02 prepared for passes.
- The UML consistency test requires every business, model and persistence class to appear in `uml/model.puml`.

## Goals / Non-Goals

**Goals:**
- Passes as their own objects, with prices, purchase rules and refunds as specified.
- Consistent cascades.
- Passes visible on the user invoice.

**Non-Goals:**
- File persistence of passes (F3-C09).
- Consuming passes on attraction entry (F3-C08).

## Decisions

1. **Model.**
   - `Pass` (abstract) holds `id`, `ticket : IndividualTicket`, `price : BigDecimal` (scale 2) and `consumed`. Its methods:
     - `consume()`;
     - `isConsumed()`;
     - `isRefundableOn(date)`: not consumed, and the ticket date is after `date`;
     - `typeName()`.
   - `SpeedPass.priceFor(ticket)` is the price × 0.20, rounded HALF_UP to 2 decimals.
   - `SpecialPass.priceFor(ticket)` is the price.
   - Navigation is pass → ticket. The ticket's passes are derived by traversing the pass repository, the same pattern as active-ticket checks. This keeps `IndividualTicket` unchanged and avoids bidirectional references.
2. **`PassRepository`.** An insertion-ordered map by identifier, with:
   - `exists`, `add`, `findById` and `remove`;
   - `findAll`;
   - `passesOf(IndividualTicket)`;
   - `specialPassOf(IndividualTicket)`.

   `ParkRepositories` gains `getPasses()`, which is not saved to files yet.
3. **`PassManager` singleton.** It is injected with `ParkController` and resolves tickets through `park.getTickets()`.
   - `buySpeedPasses(purchaseId, ticketId, quantityText)`:
     - Validates the order: non-empty id; quantity exactly `"1"` or `"10"`; the ticket is an `IndividualTicket` (otherwise `NotFoundException("no individual ticket ...")`); the date is not before the current date.
     - Builds all identifiers and rejects the purchase if any exists. Only then does it add the passes.
     - Returns the list of created passes.
   - `buySpecialPass(id, ticketId)` follows the same checks, plus "the ticket already has a Special Pass".
   - `refund(id, Class<? extends Pass> type)` looks the pass up by type and checks `isRefundableOn(currentDate)`.
4. **Cascades in `TicketManager`.**
   - All checks (existence, type, part-of-group, deletability) run first.
   - Then `park.getPasses().passesOf(ticket)` are removed, then the tickets.
   - For a group ticket, the order is passes of every component, then the components, then the group ticket.
   - Removal operations on in-memory maps cannot fail, so validating before any removal guarantees no partial deletion.
   - The `OK` response mentions how many passes were removed when there were any.
5. **Presentation `PassCommands`.** The handler holds the `PassManager` and builds these responses:
   - `OK: 1 SpeedPass created for ticket T001 (SP01:1), unit price 8.00 EUR, total 8.00 EUR`
   - `OK: 10 SpeedPasses created for ticket T001 (SP02:1 to SP02:10), unit price 8.00 EUR, total 80.00 EUR`
   - `OK: Special Pass PE01 created for ticket T001, price 40.00 EUR`
   - `OK: SpeedPass SP02:3 refunded, 8.00 EUR`

   The four syntaxes and purposes are added to `CommandCatalog` after `GROUPTICKET DELETE`.
6. **Invoice.**
   - `UserInvoiceBuilder.buildPersonalPasses()` collects the passes whose ticket holder is the user, across every individual ticket including components.
   - It adds a "SpeedPasses" section and a "Special Passes" section; each item is "SpeedPass SP01:1 for ticket T001" with the pass price, suffixed " (consumed)" when consumed.
   - Empty sections are omitted by the builder base class.
7. **UML.**
   - `Pass <|-- SpeedPass` and `Pass <|-- SpecialPass`.
   - `SpeedPass "*" --> "1" IndividualTicket : ticket` and `SpecialPass "0..1" --> "1" IndividualTicket : ticket`.
   - `PassRepository` aggregating `Pass`; `ParkRepositories *--> PassRepository`; `PassManager` with its injection.

## Risks / Trade-offs

- Passes are lost on restart until F3-C09. This is documented in the proposal and README.
- Deriving a ticket's passes by traversal is O(n) per lookup, which is negligible at this scale.
