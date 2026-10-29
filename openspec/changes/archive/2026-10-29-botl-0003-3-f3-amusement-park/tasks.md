# Tasks

## 1. Layered packages and exceptions (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.*` and `persistence` packages with repository interfaces in `business.repository`, keep `App` as entry point, and verify `mvn package` and the existing tests pass
- [x] 1.2 Introduce the `ParkException` hierarchy and `PersistenceException`, use specific exceptions at throw sites, handle them only in `CommandProcessor`/`App`, and verify with `ArchitectureTest` (packages, no presentation→persistence imports, no default package)

## 2. Singletons and dependency injection (F3-C03)

- [x] 2.1 Turn `UserService` into singleton `UserController`, add singleton `PassController` and `InvoiceController`, add `configure(...)` injection methods and the `App.bootstrap` composition root, and verify singleton identity and in-memory repository injection in `ArchitectureTest`

## 3. Password protection (F3-C05)

- [x] 3.1 Add jbcrypt 0.4 and maven-shade-plugin, implement `PasswordHasher`/`BCryptPasswordHasher`, hash on create/update and verify on delete/update, and verify in `PasswordSecurityTest` (no plaintext in file, re-hash on change, wrong password rejected)

## 4. Extended help (F3-C04)

- [x] 4.1 Add `CommandCatalog`, `HelpFormat` strategies and injected `HelpGenerator`, implement HELP and HELP EXTENDED, and verify every syntax and description in `HelpTest`

## 5. Passes (F3-C06)

- [x] 5.1 Implement `Pass`, `SpeedPass`, `SpecialPass` and `PassController` purchase/refund rules and verify prices, ids, quantity, conflicts and refunds in `PassTest`
- [x] 5.2 Cascade pass deletion from TICKET DELETE / GROUPTICKET DELETE and verify in `PassTest`

## 6. Attraction types and entry (F3-C07, F3-C08)

- [x] 6.1 Implement access interfaces, `Attraction<T extends GeneralAccess>` and the four typed attractions with `AttractionType`, accept the 5-field ATTRACTION CREATE, and verify type restrictions in `AttractionTypeTest`
- [x] 6.2 Implement SPEED entry and automatic Special Pass consumption with atomic checks and verify individual, group and special scenarios in `AttractionTypeTest`

## 7. Invoices (F3-C02)

- [x] 7.1 Implement `Invoice`, builders, `InvoiceController` and `InvoicePrinter` with INVOICE CREATE, and verify user/group contents, omitted sections and totals in `InvoiceTest`

## 8. Persistence and UML (F3-C09)

- [x] 8.1 Persist attraction types and passes (`passes.txt`), read legacy attraction lines as GENERAL, add `passes.txt` to `.gitignore`, and verify round-trips in `ParkPersistenceTest`
- [x] 8.2 Update `uml/model.puml` for the Delivery 3 design and verify required elements in `UmlModelTest`

## 9. Delivery 3 integration (F3-C10)

- [x] 9.1 Run `mvn clean package` and verify all tests pass and the shaded JAR contains BCrypt
- [x] 9.2 Run an `input_fase3.txt` scenario with the JAR in normal and `--fly` modes and verify outputs, persisted hashes/passes, and that `--fly` leaves no data
