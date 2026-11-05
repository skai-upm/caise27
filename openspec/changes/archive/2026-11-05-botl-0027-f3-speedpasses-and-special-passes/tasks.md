## 1. Model and repository

- [x] 1.1 Add `Pass` (abstract), `SpeedPass` and `SpecialPass` to `business.model`: identifier, ticket, price derived from the ticket's stored price, consumed flag, and the refundability check.
- [x] 1.2 Add `persistence.PassRepository` (shared pass identifier space, passes of a ticket, Special Pass of a ticket) and inject it through `ParkRepositories` and `ParkController`.

## 2. Business rules

- [x] 2.1 Add the `PassManager` singleton, injected by `ParkConfiguration`:
  - SpeedPass purchase: quantity 1 or 10, all identifiers checked before creating, ticket existence and date checks;
  - Special Pass purchase: one per ticket, even when consumed;
  - refunds by type: unconsumed, ticket strictly in the future.
- [x] 2.2 Cascade `TICKET DELETE` and `GROUPTICKET DELETE` to the associated passes, validating everything before removing anything.
- [x] 2.3 Fill `UserInvoiceBuilder.buildPersonalPasses()` with SpeedPasses and Special Passes sections (including passes on group components).

## 3. Commands, documentation and verification

- [x] 3.1 Add `PassCommands` and the dispatch of the four commands, and add them to `CommandCatalog` after `GROUPTICKET DELETE`.
- [x] 3.2 Update the UML (pass hierarchy, ticket relationships and cardinalities, repository, manager), the controller and repository lists in `ControllerConfigurationTest`, and README.
- [x] 3.3 Add tests:
  - prices, including the discounted component and `SYSTEM PRICE` not being retroactive;
  - SpeedPass 1 and 10, invalid quantity, identifier conflict without partial creation, unknown, group and past tickets;
  - Special Pass uniqueness, including after consumption, and repurchase after a refund;
  - refunds: future unconsumed, consumed, today's ticket, wrong type;
  - cascades on individual and group ticket deletion, and a rejected deletion keeping its passes;
  - invoice sections.
- [x] 3.4 Run `mvn package` and an end-to-end JAR regression: output identical to the baseline except for the HELP lines, plus a pass scenario run.
