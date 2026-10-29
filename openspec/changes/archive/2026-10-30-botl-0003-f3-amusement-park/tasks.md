# Tasks

## 1. Layered reorganisation and exceptions (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.*` and `persistence` packages (tests mirrored) and verify `mvn -q test` still passes with Delivery 2 behaviour
- [x] 1.2 Introduce the `business.exception` hierarchy (replacing `OperationException`), `CommandSyntaxException` in presentation and runtime-exception containment in `CommandProcessor`; verify `ArchitectureTest` (layer packages, presentation imports no persistence/`Files`, model has no file I/O) and a processor test for contained runtime errors

## 2. Singleton controllers and dependency injection (F3-C03)

- [x] 2.1 Define repository interfaces and `Repositories` in `business.repository`, make file repositories implement them, rename `ParkStorage` to `StorageConfiguration`; verify repositories have public constructors and no static instance
- [x] 2.2 Split `Park` into singleton `SystemController`, `UserController`, `GroupController`, `TicketController`, `AttractionController` behind the `Park` facade, wired by `ParkInitializer`; verify all Delivery 1–2 Park tests pass, controller singleton tests, and an in-memory repository test that writes no files

## 3. Password protection (F3-C05)

- [x] 3.1 Add `jbcrypt` 0.4 and `maven-shade-plugin`; add `PasswordHasher`/`BCryptPasswordHasher`; store hashes in `User`/`users.txt` and verify against them; verify `PasswordProtectionTest` (hash stored, plain absent, verify after restart, password change, invalid password rejected)

## 4. Extended help (F3-C04)

- [x] 4.1 Add `CommandCatalog`, `HelpGenerator` with injected `HelpFormatter` (`SyntaxHelpFormatter`, `ExtendedHelpFormatter`) and the `HELP EXTENDED` command; verify `HelpGeneratorTest` (all syntax entries incl. Delivery 3, descriptions in extended, custom formatter injection)

## 5. Passes (F3-C06)

- [x] 5.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassController` with `SPEEDPASS CREATE/DELETE`, `SPECIALPASS CREATE/DELETE`; verify `PassControllerTest` for prices (incl. discounted and after SYSTEM PRICE), naming, quantity, atomic id conflicts, one Special Pass, refunds and rejections
- [x] 5.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE`; verify tests for both cascades and for rejected deletions leaving passes intact

## 6. Attraction types and access (F3-C07, F3-C08)

- [x] 6.1 Add access interfaces implemented by tickets, generic `Attraction<T>` with four subclasses and `AttractionType`, typed `ATTRACTION CREATE`; verify `AttractionTypeTest` for creation, unknown type, and accepted/rejected ticket kinds per type
- [x] 6.2 Extend `ATTRACTION ENTER` with `SPEED` and automatic Special Pass consumption (atomic, output names consumed passes); verify tests for individual/group SPEED success and failure (no state change), special success, and every special rejection

## 7. Invoices (F3-C02)

- [x] 7.1 Add `Invoice`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceDirector`, `InvoiceController`, `InvoicePrinter` and `INVOICE CREATE;USER|GROUP;…`; verify `InvoiceTest` for the user example total 96.00, the group example total 192.00 excluding passes, omitted empty sections, and error cases

## 8. Persistence and UML (F3-C09)

- [x] 8.1 Persist attraction types and passes (`passes.txt`), legacy 3-field attraction lines as GENERAL; verify `ParkPersistenceTest` reconstructs types and passes with consumed state and that `--fly` leaves no data
- [x] 8.2 Rewrite `uml/model.puml` for the Delivery 3 design and update `UmlModelTest` to cover all `business.model` types plus controllers, builder and access interfaces

## 9. Delivery 3 integration (F3-C10)

- [x] 9.1 Extend `HELP` with all Delivery 1–3 syntax; run `mvn clean package` and verify all tests pass and the shaded `target/nombre-aplicacion.jar` is produced
- [x] 9.2 Run the JAR from a scratch directory with an `input_fase3.txt` exercising every Delivery 3 command in normal and `--fly` modes; verify outputs, hashed `users.txt`, `passes.txt`, and no leftover `tmp/`
- [x] 9.3 Verify no regex/serialization usage in main sources and that `jbcrypt` is the only non-test dependency
