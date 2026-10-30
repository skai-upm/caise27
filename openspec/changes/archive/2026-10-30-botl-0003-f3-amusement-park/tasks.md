# Tasks

## 1. Layered reorganization and exceptions (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.*` and `persistence` packages (tests mirrored) and verify `mvn test` passes unchanged in behaviour
- [x] 1.2 Introduce `business.exception` hierarchy and `persistence.PersistenceException`, replace `IllegalArgumentException`/`UncheckedIOException` usage, and verify tests assert the specific exceptions and `CommandProcessor` still prints `ERROR:` and continues

## 2. Singletons and dependency injection (F3-C03)

- [x] 2.1 Make every controller a Singleton with `configure(...)`, add `persistence.Repositories` and `ApplicationInitializer` wiring (normal and `--fly`), and verify `ApplicationInitializerTest` (same instances, reconfiguration to a new directory)

## 3. Password protection (F3-C05)

- [x] 3.1 Add jbcrypt 0.4 and maven-shade-plugin, `PasswordHasher`/`BCryptPasswordHasher`, hash on create/update and verify on delete/update; verify `PasswordSecurityTest` (file contains hash not password, re-hash on change, verify after restart) and that the shaded JAR runs

## 4. Extended help (F3-C04)

- [x] 4.1 Add `HelpFormatter` strategies, `HelpGenerator` with injected formatters, command descriptions, multi-variant registrations and `HELP EXTENDED`; verify `HelpGeneratorTest` and `CommandProcessorTest` (all required syntaxes, descriptions present)

## 5. Passes (F3-C06)

- [x] 5.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassRepository`/`PassFileStore` and `PassController` (create, refund, lookup); verify `PassControllerTest` (pricing incl. group discount, quantity rules, atomic ids, one special pass, refund rules)
- [x] 5.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE`; verify `TicketControllerTest` cascade cases

## 6. Generic attraction types and pass consumption (F3-C07, F3-C08)

- [x] 6.1 Add access interfaces, generic `Attraction<T>` hierarchy, `AttractionType`, typed creation and persistence with legacy lines; verify `AttractionControllerTest` type acceptance and legacy load
- [x] 6.2 Extend entry with `SPEED` and automatic Special Pass consumption, all-or-nothing; verify `AttractionControllerTest` SpeedPass/Special Pass success and rejection cases leave passes unchanged

## 7. Invoices (F3-C02)

- [x] 7.1 Add `Invoice`, `InvoiceSection`, `InvoiceItem`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceController`; verify `InvoiceControllerTest` (user totals with passes on composition tickets, group totals excluding passes, omitted empty sections, unknown recipients)

## 8. Persistence, UML and integration (F3-C09, F3-C10)

- [x] 8.1 Verify persistence of passes (price, ticket, consumed) and attraction types across restart and in `--fly` (`AppTest`)
- [x] 8.2 Update `uml/model.puml` for the Delivery 3 design and verify every named class/interface exists in `src/main/java`
- [x] 8.3 Run `mvn clean package`, then the JAR with a Delivery 3 command file in normal and `--fly` modes; verify outputs and that `--fly` leaves no data
