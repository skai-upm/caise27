# Tasks

## 1. Layered reorganisation and exceptions (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.{model,controller,exception,validation}` and `persistence`, replace `UserException` with the `ParkException` hierarchy handled in `CommandProcessor`; verify the full Delivery 1–2 test suite passes after moving the tests

## 2. Singleton controllers and dependency injection (F3-C03)

- [x] 2.1 Split state into singleton `SystemController`, `UserController`, `GroupController`, `TicketController`, `AttractionController` with `initialize(...)` injection, make `ParkController` the cross-entity coordinator, add repository interfaces and wire everything in `App` (normal and `--fly` differ only by `ParkStorage` directory); verify controller and app tests pass and each controller's `getInstance()` is stable

## 3. Password protection (F3-C05)

- [x] 3.1 Add jBCrypt 0.4 and the shade plugin to `pom.xml`, add `PasswordHasher`/`BCryptPasswordHasher`, store hashes and verify with them in create/update/delete; verify tests for hash-only storage, verification after reload and password change, and that the shaded JAR runs standalone

## 4. Passes (F3-C06)

- [x] 4.1 Add `Pass`, `SpeedPass`, `SpecialPass`, ticket↔pass association and `PassController` with `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` (quantity 1|10, derived ids, atomic id check, pricing, one Special Pass per ticket, refund rules); verify pass tests
- [x] 4.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE` with no partial changes on rejection; verify cascade tests

## 5. Attraction types and access (F3-C07, F3-C08)

- [x] 5.1 Add `AccessTicket`/`IndividualAccess`/`GroupAccess`, generic `Attraction<T>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; `ATTRACTION CREATE` with optional type; verify accepted-kind tests per type
- [x] 5.2 Extend `ATTRACTION ENTER` with `;SPEED` (one pass per individual ticket, atomic for groups) and automatic Special Pass consumption; verify speed/special entry tests including failure leaving passes untouched

## 6. Invoices and help (F3-C02, F3-C04)

- [x] 6.1 Add `business.invoice` Builder classes and `InvoiceController`, `INVOICE CREATE;USER|GROUP;<id>`; verify invoice tests (Ana total 167.20, G01 total 108.00, omitted empty sections, unknown targets)
- [x] 6.2 Add `presentation.help` (`CommandCatalog`, `HelpFormatter`, syntax/extended formatters, injected `HelpGenerator`), `HELP EXTENDED` and the Delivery 3 syntax lines; verify help tests

## 7. Persistence and UML (F3-C09)

- [x] 7.1 Persist attraction types (3-field lines → GENERAL) and passes (`passes.txt`), load them after tickets, include them in `--fly`; verify reload tests
- [x] 7.2 Update `uml/model.puml` and `README.md` for the final design; verify UML class names match the source

## 8. Delivery 3 integration (F3-C10)

- [x] 8.1 Run `mvn package`, run the assignment's example (`input_fase3.txt`) with and without `--fly` from a copied JAR; verify outputs (SP01 total 80.00, PE01 40.00, SpeedPass/Special Pass consumption messages, invoices), that `--fly` leaves no data and that a normal run reloads passes and types
