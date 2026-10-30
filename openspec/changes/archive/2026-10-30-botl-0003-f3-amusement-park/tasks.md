# Tasks

## 1. Layers, exceptions, singletons and injection (F3-C01, F3-C03)

- [x] 1.1 Move classes into `presentation`, `business.{model,controller,exception,...}` and `persistence(.file)`; add the `ParkException` hierarchy and `PersistenceException`; replace `IllegalArgumentException` rejections; verify `mvn test` passes with tests updated to the new types
- [x] 1.2 Turn repositories into interfaces with `File*Repository` implementations created by `FileRepositories`; make controllers Singletons configured by `ApplicationInitializer`; verify `ArchitectureTest` (controller uniqueness, new repository instances per initialisation, presentation not referencing the persistence package)

## 2. Passwords (F3-C05)

- [x] 2.1 Add jBCrypt 0.4 and maven-shade; add `PasswordHasher`/`BCryptPasswordHasher`; hash on create/update and verify with BCrypt; verify `PasswordSecurityTest` (file has no plain password, hash verifies, re-hash on change, wrong password rejected) and that the shaded JAR runs

## 3. Passes (F3-C06)

- [x] 3.1 Add `Pass`/`SpeedPass`/`SpecialPass`, `PassRepository` (passes.txt) and `PassController` purchase/refund rules; verify `PassControllerTest` (prices incl. discounted composed ticket, quantity 1/10, ids, atomic conflict, one Special Pass, refund rules)
- [x] 3.2 Cascade pass deletion from TICKET DELETE and GROUPTICKET DELETE; verify `PassControllerTest` cascade scenarios

## 4. Typed attractions and entry (F3-C07, F3-C08)

- [x] 4.1 Add access interfaces, generic `Attraction<T>` with GENERAL/INDIVIDUAL/GROUP/SPECIAL subclasses and typed ATTRACTION CREATE (3-arg form = GENERAL); verify `AttractionTypeTest` acceptance matrix
- [x] 4.2 Extend ENTER with SPEED and automatic Special Pass consumption, atomic for groups; verify `AttractionTypeTest` consumption, atomic failure, special rejections and output naming consumed passes

## 5. Invoices and help (F3-C02, F3-C04)

- [x] 5.1 Add the invoice builder classes, `InvoiceController` and `INVOICE CREATE;USER|GROUP;...`; verify `InvoiceTest` (user totals exclude composed tickets but include their passes, empty sections omitted, group total excludes passes, unknown user/group)
- [x] 5.2 Add `CommandCatalog`, `HelpGenerator` with injected `HelpFormatter`, HELP (all deliveries) and HELP EXTENDED; verify `CommandProcessorTest` HELP/HELP EXTENDED content

## 6. Persistence, UML and integration (F3-C09, F3-C10)

- [x] 6.1 Persist attraction types (legacy 3-field lines → GENERAL) and passes with price/consumed; verify `ParkPersistenceTest` restart scenarios and `--fly` coverage in `AppTest`
- [x] 6.2 Update `uml/model.puml` for Delivery 3 and `UmlModelTest` to resolve classes across packages; verify the test passes
- [x] 6.3 Run `mvn clean package` and the JAR end-to-end with a Delivery 3 command file in normal and `--fly` mode; verify outputs and no leftover data; update `README.md`
