# Tasks

## 1. Layering, exceptions and dependency injection

- [x] 1.1 Move classes into `presentation`, `business.{model,controller,validation,exception,security}` and `persistence(.file)` packages (tests moved accordingly); verify `mvn package` and the Delivery 1–2 tests pass unchanged in behavior
- [x] 1.2 Introduce the `ParkException` hierarchy and replace `UserOperationException`; verify `CommandProcessor` prints `ERROR:` for each exception type and keeps processing (unit test)
- [x] 1.3 Introduce generic `Repository<T>`, repository interfaces, `ListRepository<T>`, file implementations and `FileRepositories.open(DataPaths)`; verify save/reload tests pass and two `open` calls return distinct instances
- [x] 1.4 Convert services into singleton controllers with `configure(...)` injection wired by `Park.configure(Repositories, PasswordHasher)` and `App`; verify singleton identity and unconfigured-use failure with unit tests, and `--fly` tests still pass

## 2. Password protection

- [x] 2.1 Add jBCrypt and the shade plugin to `pom.xml`, `PasswordHasher`/`BCryptPasswordHasher`, hash on create/password change, verify on delete/update; verify with unit tests (no plain text in `users.txt`, verification after reload, invalid stored hash never matches) and that `java -jar target/amusement-park.jar` runs

## 3. Passes

- [x] 3.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassRepository`, `PassFileStore` (`passes.txt`); verify price rules (8.00 / 7.20 / 40.00) and save/reload of consumed state with unit tests
- [x] 3.2 Implement `PassController` purchase (quantity 1/10, derived ids, all-or-nothing, eligibility, one Special Pass) and refunds; verify every passes scenario with unit tests
- [x] 3.3 Cascade pass deletion in `TICKET DELETE` and `GROUPTICKET DELETE`; verify with unit tests that passes from several purchases and component passes are removed

## 4. Attraction types and access

- [x] 4.1 Add access interfaces, generic `Attraction<T extends Access>` with the four typed subclasses and `AttractionType`, typed persistence (missing type → GENERAL); verify type acceptance and Delivery 2 file compatibility with unit tests
- [x] 4.2 Extend `AttractionController.enter` with `SPEED` and automatic Special Pass consumption (atomic); verify individual/group SpeedPass entry, missing pass, special entry, SPEED at special, and that failed entries change no pass

## 5. Invoices

- [x] 5.1 Add `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceController` and `InvoicePrinter`; verify the user (167.20) and group (108.00) scenarios and omitted empty sections with unit tests

## 6. Commands and help

- [x] 6.1 Register the new commands (multi-arity `ATTRACTION CREATE`/`ENTER`, passes, `INVOICE CREATE`) with descriptions; add `HelpFormatter` strategies and injected `HelpGenerator`s for `HELP` and `HELP EXTENDED`; verify with unit tests that both outputs list every syntax and extended help has descriptions

## 7. Documentation and integration

- [x] 7.1 Update `uml/model.puml` (access interfaces, generic attractions and types, passes with cardinalities, builders, controllers, DI relations) and `README.md`; add `passes.txt` to `.gitignore`; verify every new model class appears in the diagram
- [x] 7.2 Run `mvn package` and the assignment's Delivery 3 example sequence through the JAR in normal mode (twice, checking persistence) and with `--fly`; verify outputs match the specs and all tests pass
