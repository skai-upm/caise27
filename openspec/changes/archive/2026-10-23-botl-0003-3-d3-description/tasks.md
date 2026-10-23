# Tasks

## 1. Layered reorganization (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller`, `business.validation`, and `persistence` (`App` stays at the root), then update imports in main and test code; verify `mvn clean package` passes the full existing suite and the JAR still runs

## 2. Exceptions, Singleton controllers and dependency injection (F3-C03)

- [x] 2.1 Introduce the `business.exception` hierarchy (`ParkException`, `InvalidDataException`, `NotFoundException`, `OperationNotAllowedException`, `PersistenceException`) replacing `UserException`; verify existing error-path tests still pass and a new test checks the exception types raised for invalid data, missing entities, and disallowed operations
- [x] 2.2 Split `Park` into Singleton controllers (`SystemController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`) with `configure(...)` injection of repository interfaces, the `TransactionManager` (`FileDataStore`), and collaborating controllers, plus `Park.load()`; make `App` the composition root; verify all tests pass, and new tests check singleton identity, that repositories are not singletons, and that two different data directories can be wired into the same controllers

## 3. Invoices with Builder (F3-C02)

- [x] 3.1 Add `business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceItem`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`), the `InvoiceController` director, `InvoiceFormatter`, and `INVOICE CREATE;USER|GROUP;...` (tickets only for now); verify builder unit tests (empty sections omitted, totals, non-counted details) and command tests for user and group invoices

## 4. Extended help (F3-C04)

- [x] 4.1 Add `CommandInfo` descriptions for every command, `HelpGenerator` with injected `HelpFormatter` (`SyntaxHelpFormatter`, `ExtendedHelpFormatter`), and the `HELP EXTENDED` command, with multiple syntaxes per keyword; verify tests that HELP lists every syntax and HELP EXTENDED adds a description to each

## 5. Password protection (F3-C05)

- [x] 5.1 Add the jBCrypt dependency and `maven-shade-plugin`, `PasswordHasher` / `BCryptPasswordHasher` injected into `UserController`, store only hashes, verify credentials with BCrypt, and skip non-hash password lines on load; verify tests for hashing, verification, password change, file content without plain passwords, plain-text line skipping, and that the shaded JAR contains the BCrypt classes

## 6. SpeedPasses and Special Passes (F3-C06)

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassController` (`PassWallet`), the `passes.txt` repository, and the `SPEEDPASS CREATE|DELETE` and `SPECIALPASS CREATE|DELETE` commands (prices, quantities 1 or 10, all-or-nothing ids, one Special Pass per ticket, refund rules); verify with model, controller, persistence, and command tests
- [x] 6.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE` in a single transaction with undo, and add passes to user invoices (excluding group components as tickets); verify cascade tests (including after restart) and the user invoice total from the source example (167.20)

## 7. Generic attraction types (F3-C07)

- [x] 7.1 Add the access interfaces implemented by tickets, generic `Attraction<T extends GeneralAccess>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, and `SpecialAttraction`, `AttractionType`, the typed `ATTRACTION CREATE` (default GENERAL), type persistence (old 3-field lines as GENERAL), and the type in STATUS; verify ticket-acceptance tests per type and reflection tests of the generic bounds

## 8. Entry and pass consumption (F3-C08)

- [x] 8.1 Extend `ATTRACTION ENTER` with the optional `SPEED` option and automatic Special Pass use through two-phase atomic entry (checks, then enter plus consume plus persist, undo on failure), reporting the consumed passes; verify individual and group speed entry, missing-pass all-or-nothing, special entry, SPEED rejected at special attractions, and persistence of consumed flags

## 9. Persistence and UML (F3-C09)

- [x] 9.1 Verify a full Delivery 3 round trip (attraction types, passes with prices and consumed flags, hashes) and that `--fly` stores and cleans up `passes.txt`; add any missing coverage
- [x] 9.2 Update `uml/model.puml` to the Delivery 3 design (packages, controllers with `<<singleton>>`, access interfaces, generic attractions, passes with multiplicities, invoice builders, injected repositories and formatter) and extend `UmlModelTest` (generic bounds, interface implementations); verify the test passes and PlantUML `-checkonly` succeeds

## 10. Delivery 3 integration (F3-C10)

- [x] 10.1 Update `README.md` (new commands, attraction types, passes, invoices, BCrypt, architecture); verify the documented syntax matches HELP
- [x] 10.2 Run `mvn clean package` (all tests green) and execute the source example `input_fase3.txt` with the shaded JAR normally and with `--fly` from a scratch directory; verify pass prices (8.00 each, 80.00 for ten, PE01 40.00), consumed-pass reporting, A03 group speed entry, Ana's invoice total 167.20 and G01's 108.00, hashes in `users.txt`, and no leftovers after `--fly`
