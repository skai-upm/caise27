# Tasks

## 1. Layers and exceptions (F3-C01)

- [x] 1.1 Move classes into `presentation`, `business.*`, `persistence.*` packages (App as composition root), add the `ParkException` hierarchy and replace `IllegalArgumentException` uses; verify `mvn test` passes with all Delivery 1/2 tests moved to the matching packages
- [x] 1.2 Add an architecture test asserting no presentation class imports `persistence` and no default-package classes; verify it passes

## 2. Singletons and dependency injection (F3-C03)

- [x] 2.1 Turn repositories into interfaces with `Text*` implementations, make `ParkController`, `UserController`, `PassController`, `InvoiceController` singletons configured by `AppInitializer`; verify `ArchitectureTest` (same instances, private constructors) and that `--fly` tests still pass

## 3. Password protection (F3-C05)

- [x] 3.1 Add jbcrypt dependency and shade plugin, `PasswordHasher`/`BCryptPasswordHasher`, hash on create/password update, verify on delete/update; verify `UserControllerTest` (hash stored, no plain text, rehash on change, wrong password rejected, invalid new password not stored)

## 4. Passes (F3-C06)

- [x] 4.1 Implement `Pass`/`SpeedPass`/`SpecialPass` owned by `IndividualTicket`, `PassController` purchase (1|10, ids, atomic conflict check, prices, date) and refunds; verify `PassTest`
- [x] 4.2 Cascade pass deletion on `TICKET DELETE`/`GROUPTICKET DELETE`; verify tests for both cascades

## 5. Generic attraction types and entry (F3-C07, F3-C08)

- [x] 5.1 Implement access interfaces, `Attraction<T>` with four subclasses, `AttractionType`, typed `ATTRACTION CREATE` (4/5 fields); verify type-acceptance tests
- [x] 5.2 Implement `SPEED` entry (one pass per entry ticket, atomic for groups, output names passes) and SPECIAL attraction Special Pass consumption; verify tests for success, missing pass, partial group passes, SPEED at SPECIAL, consumed Special Pass

## 6. Invoices (F3-C02)

- [x] 6.1 Implement `InvoiceBuilder` hierarchy, `Invoice` rendering and `InvoiceController` director with `INVOICE CREATE;USER|GROUP`; verify `InvoiceTest` (84.00 user example, 108.00 group example, omitted sections, unknown recipient)

## 7. Help (F3-C04)

- [x] 7.1 Implement `CommandCatalog`, `HelpGenerator` with injected `HelpFormatter` (syntax / extended), `HELP` and `HELP EXTENDED`; verify tests that HELP lists all Delivery 1–3 syntaxes and HELP EXTENDED includes descriptions

## 8. Persistence and UML (F3-C09)

- [x] 8.1 Persist attraction types (legacy 3-field lines as GENERAL) and passes with price and consumed state; verify `ParkPersistenceTest` reload cases
- [x] 8.2 Update `uml/model.puml` for Delivery 3 and verify with PlantUML `-checkonly` and a cross-check against the source

## 9. Integration (F3-C10)

- [x] 9.1 Run `mvn clean package`, then run the shaded JAR with an `input_fase3.txt` covering all Delivery 3 commands in normal and `--fly` modes; verify outputs, hashed `users.txt`, persisted passes, and no `--fly` residue
