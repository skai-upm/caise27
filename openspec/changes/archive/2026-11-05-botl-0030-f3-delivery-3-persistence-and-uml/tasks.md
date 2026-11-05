## 1. Pass persistence

- [x] 1.1 Add restore constructors with a stored price to `SpeedPass` and `SpecialPass`.
- [x] 1.2 `ParkFileStore`:
  - add `passes.txt` (`S|P;id;ticketId;price;consumed`), saved together with the other park files;
  - load it after the tickets, with full validation, counting malformed lines.
- [x] 1.3 `ParkRepositories`: load and save passes through the store, and remove the in-memory note. Add `passes.txt` to the `ParkSession` unreadable-data message and to `.gitignore`.

## 2. UML

- [x] 2.1 Review `uml/model.puml` against the section 14 checklist and add the `ParkFileStore` to `PassRepository` relationship. Extend `UmlModelConsistencyTest` with the Delivery 3 required relationships, and check the diagram with PlantUML.

## 3. Verification and documentation

- [x] 3.1 Add tests:
  - passes restored with ticket, price, state and group components, after a `SYSTEM PRICE` change;
  - consumed Special Pass still blocks a repurchase after a restart;
  - refunds and cascades not restored;
  - malformed pass lines ignored and counted;
  - `--fly` writes `passes.txt` only in the temporary directory.
- [x] 3.2 Update README (the `passes.txt` format; remove the in-memory note).
- [x] 3.3 Run `mvn package` and an end-to-end JAR regression, plus a restart scenario with passes.
