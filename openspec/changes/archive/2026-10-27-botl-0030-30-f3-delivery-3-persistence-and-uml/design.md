# Design

## Decisions

- **`passes.txt` format**: `SPEED;id;ticketId;price;consumed` / `SPECIAL;id;ticketId;price;consumed` (`consumed` = `true|false`). Loading resolves `ticketId` with `TicketRepository.findIndividualTicket`; unresolved or malformed records are skipped.
- **Restoring constructor**: `Pass(id, ticket, price, consumed)` (and in `SpeedPass`/`SpecialPass`) so stored prices are reused instead of recomputed.
- **`PassRepository(PassFileStore, TicketRepository)`**: loads at creation; `addAll`/`remove` save; `save()` public for consumption done on model objects. `AttractionController.enter` saves passes when any were consumed; each `remove` performed by `PassController.removePassesOf` saves through the repository.
- **Initializer**: users → groups → tickets → passes → attractions, from `DataPaths` (permanent or temporary) — no mode-specific repository.
- **UML**: rewritten by layer packages; `<<singleton>>` stereotypes on controllers; `ApplicationInitializer ..> X : injects`; generic binding shown as `<<bind>>` labels; cardinalities on ticket→pass relations. `UmlModelTest` scans `business/model`, `business/controller` and `business/invoice`.
