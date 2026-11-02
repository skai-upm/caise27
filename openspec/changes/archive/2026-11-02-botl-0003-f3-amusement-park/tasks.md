# Tasks

## 1. F3-C01 — Layered packages

- [x] 1.1 Move classes into `presentation`, `business` and `persistence` packages (entry point in `es.upm.etsisi.park`), introduce the business `ParkRepository` interface + `ParkData` implemented by `persistence.TextParkRepository`, and the exception hierarchy; port the Delivery 1/2 tests to the new packages and verify they all pass
- [x] 1.2 Add an architecture test asserting no default-package class, no `persistence` import in `business`/`presentation` main code and no `java.nio.file.Files` use outside `persistence`; verify it passes
- [x] 1.3 Checkpoint F3-C01 (`[F3-C01] Reorganize application into layered packages`): verify `mvn package` succeeds (milestone commit owned by the BOTL harness, see design.md)

## 2. F3-C03 — Singleton controllers and injection (implemented before invoices because invoices are a controller)

- [x] 2.1 Split `Park` rules into singleton `UserController`, `GroupController`, `TicketController`, `AttractionController` around the `Park` state singleton, wired by `ParkApplication.start(repository, date, hasher)`; verify `ControllerWiringTest` (same instances, private constructors, an in-memory `ParkRepository` works unchanged) and all regression tests pass
- [x] 2.2 Checkpoint F3-C03 (`[F3-C03] Add singleton controllers and dependency injection`): verify `mvn package` succeeds

## 3. F3-C02 — Invoices with Builder

- [x] 3.1 Implement `business.invoice` (`InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`, `Invoice`, `InvoiceSection`, `ChargeLine`, `DetailLine`) and the singleton `InvoiceController`, plus `INVOICE CREATE;USER;<email>` / `INVOICE CREATE;GROUP;<id>`; verify `InvoiceTest` covers direct ticket vs constituent, empty sections omitted, group total counted once, unknown user/group
- [x] 3.2 Checkpoint F3-C02 (`[F3-C02] Add invoice generation with builder`): verify `mvn package` succeeds

## 4. F3-C04 — Extended help

- [x] 4.1 Replace the command `switch` with a `CommandDefinition` registry and add `HelpGenerator` with injected `SyntaxHelpFormatter` / `ExtendedHelpFormatter`, and `HELP EXTENDED`; verify `HelpTest` checks every syntax line in HELP and a description for every command in HELP EXTENDED
- [x] 4.2 Checkpoint F3-C04 (`[F3-C04] Add extended help generation`): verify `mvn package` succeeds

## 5. F3-C05 — BCrypt passwords

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin, `PasswordHasher`/`BCryptPasswordHasher`, hashing on create and password update and hash verification on delete/update; verify `PasswordTest` (file has no plaintext, hash verifies, old password rejected after change incl. restart, invalid password still rejected, legacy plaintext never authenticates) and that `java -jar target/nombre-aplicacion.jar` still starts
- [x] 5.2 Checkpoint F3-C05 (`[F3-C05] Secure stored user passwords`): verify `mvn package` succeeds

## 6. F3-C06 — Speed and Special passes

- [x] 6.1 Implement `Pass`, `SpeedPass`, `SpecialPass` and the singleton `PassController` with SPEEDPASS/SPECIALPASS CREATE (pricing, 1|10, derived ids, all-or-nothing, one Special Pass per ticket) and DELETE (unused, strictly future); verify `PassTest`
- [x] 6.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE` through the injected `PassController`, all-or-nothing on save failure; verify cascade and rollback tests
- [x] 6.3 Checkpoint F3-C06 (`[F3-C06] Add speed and special passes`): verify `mvn package` succeeds

## 7. F3-C07 — Generic attraction types

- [x] 7.1 Add `AccessTicket`/`IndividualAccess`/`GroupAccess`, generic `Attraction<T>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, and the optional `<type>` in ATTRACTION CREATE (default GENERAL); verify `AttractionTypeTest` covers each accepted/rejected ticket kind, old syntax, unknown type and the generic bounds
- [x] 7.2 Checkpoint F3-C07 (`[F3-C07] Add generic attraction access types`): verify `mvn package` succeeds

## 8. F3-C08 — Pass consumption on entry

- [x] 8.1 Extend ATTRACTION ENTER with `;SPEED` and automatic Special Pass consumption (checks first, then admit + consume + persist with undo); verify `PassEntryTest` covers individual SPEED, group SPEED with a member lacking a pass (no change), capacity failure leaves passes, special entry consumes, special without/consumed pass and SPEED at SPECIAL rejected, consumption persisted
- [x] 8.2 Checkpoint F3-C08 (`[F3-C08] Extend attraction access and pass consumption`): verify `mvn package` succeeds

## 9. F3-C09 — Persistence and UML

- [x] 9.1 Persist passes (`passes.txt`) and attraction type (legacy 3-field lines → GENERAL) through `TextParkRepository`; verify `PersistenceTest` round-trips passes with ticket/price/consumed state and types, including `--fly`
- [x] 9.2 Rewrite `uml/model.puml` for the Delivery 3 design; verify PlantUML `-syntax` reports no error and review it against the code
- [x] 9.3 Checkpoint F3-C09 (`[F3-C09] Extend delivery 3 persistence and UML`): verify `mvn package` succeeds

## 10. F3-C10 — Delivery 3 integration

- [x] 10.1 Add an end-to-end `input_fase3`-style test mixing Delivery 1–3 commands in file, stdin and `--fly` modes; verify outputs, state and fly cleanup
- [x] 10.2 Checkpoint F3-C10 (`[F3-C10] Complete delivery 3 integration`): verify a fresh `mvn clean package` passes all tests, runtime dependencies are only jbcrypt, and the JAR runs with a file and with `--fly` a file
