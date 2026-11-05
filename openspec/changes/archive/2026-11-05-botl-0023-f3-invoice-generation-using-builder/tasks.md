## 1. Invoice product and builders

- [x] 1.1 Add `Invoice`, `InvoiceSection` and `InvoiceItem` to `business.model`. They are immutable, and the total is computed from the items.
- [x] 1.2 Add the abstract `InvoiceBuilder` to `business`:
  - steps `buildRecipient`, `buildPurchases` and `buildPersonalPasses` (default no-op);
  - `getResult`;
  - helpers that omit empty sections.
- [x] 1.3 Add `UserInvoiceBuilder`: direct individual tickets only, with group-ticket components excluded.
- [x] 1.4 Add `GroupInvoiceBuilder`: group tickets with their composition as detail lines, each counted once.
- [x] 1.5 Add the `InvoiceManager` director: resolve the recipient (`NotFoundException`) and run the steps in order.

## 2. Command interface

- [x] 2.1 Add `InvoiceCommands`: syntax validation for `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`, and generic text rendering.
- [x] 2.2 Dispatch `INVOICE CREATE` in `CommandProcessor` and add both syntaxes to HELP before `HELP` and `EXIT`. Update the tests that pin the HELP list.

## 3. Documentation and verification

- [x] 3.1 Update `uml/model.puml` with the invoice classes and the Builder relationships, and check it with PlantUML.
- [x] 3.2 Document the invoice commands in README.md.
- [x] 3.3 Add tests for:
  - the user invoice (direct tickets, excluded components, empty invoice, unknown user);
  - the group invoice (single counting, composition, members' own tickets excluded, empty, unknown group);
  - syntax errors;
  - unchanged data files.
- [x] 3.4 Run `mvn package` and an end-to-end JAR run, with regression on the existing Delivery 1 and 2 behaviour.
