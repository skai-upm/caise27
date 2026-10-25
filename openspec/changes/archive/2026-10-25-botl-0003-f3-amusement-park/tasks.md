# Tasks

## 1. [F3-C01] Layered packages

- [x] 1.1 Move classes into `presentation`, `business` (model, controller, validation, repository interfaces, exceptions) and `persistence` packages, with `App`/`ParkApplication` at `es.upm.parque`; repositories implement business interfaces; verify every existing test passes (with updated imports/accessors) and an architecture test checks packages and forbidden imports

## 2. [F3-C03] Singleton controllers and dependency injection

- [x] 2.1 Turn managers into Singleton controllers (`UserController`, `GroupController`, `TicketController`, `AttractionController`, plus `ParkController`) configured by `ParkApplication.start(dataDir, hasher)` with injected repositories and controller dependencies; move cross-entity rules into them and message formatting into presentation command modules; verify tests for Singleton identity, no repository singletons, and that `--fly` runs through the same wiring

## 3. [F3-C04] Help

- [x] 3.1 Add `CommandDefinition` descriptions, multi-arity registry, `HelpGenerator` with injected `HelpFormatter` (concise/extended) and the `HELP EXTENDED` command; verify tests that HELP shows all syntaxes and HELP EXTENDED shows syntax plus description for every command

## 4. [F3-C05] Password security

- [x] 4.1 Add jBCrypt to `pom.xml`, `PasswordHasher`/`BCryptPasswordHasher`, hash on create/password update, verify on delete/update, migrate plaintext on load; verify tests for stored hash, wrong/correct password, password change after restart and legacy migration

## 5. [F3-C06] Personal passes

- [x] 5.1 Implement `Pass`/`SpeedPass`/`SpecialPass`, `PassRepository` + text implementation (`passes.txt`) and `PassController` with purchase (1 or 10, pricing, one Special Pass) and refund rules, and `SPEEDPASS`/`SPECIALPASS` CREATE/DELETE commands; verify tests for every personal-passes purchase/refund scenario and persistence of price, ticket and consumed status

## 6. [F3-C07] Attraction types

- [x] 6.1 Implement `AccessCredential`/`IndividualAccess`/`GroupAccess`, generic `Attraction<T>` with the four bounded subclasses and `AttractionType`, typed `ATTRACTION CREATE`, type persistence with legacy GENERAL default and type shown in status; verify tests for admission by type and creation with/without type

## 7. [F3-C08] Pass consumption on entry

- [x] 7.1 Implement `ATTRACTION ENTER ...;SPEED` and SPECIAL automatic Special Pass consumption with atomicity and output reporting; verify tests for individual/group SPEED, atomic failure without consumption, special entry, missing/used special pass and SPEED rejected at SPECIAL

## 8. [F3-C02] Invoices

- [x] 8.1 Implement `Invoice`/`InvoiceSection`/`InvoiceLine`, `InvoiceBuilder` + `StandardInvoiceBuilder`, `InvoiceController` (director) and `INVOICE CREATE;USER|GROUP;...` rendering; verify tests for user invoice (direct tickets, passes incl. those of group-generated tickets, no double counting, total 88.00 example), group invoice and omitted empty sections

## 9. [F3-C09] Persistence cascade and UML

- [x] 9.1 Make `TICKET DELETE` and `GROUPTICKET DELETE` cascade to passes (checks before mutation); verify tests for cascade, failed deletion leaving passes, and `--fly` with passes
- [x] 9.2 Update `uml/model.puml` to the E3 design (packages, interfaces, generics, pass hierarchy, controllers, repository interfaces); verify with PlantUML `-checkonly` and a field-by-field review against the code

## 10. [F3-C10] Integration

- [x] 10.1 Configure the shade plugin so `mvn package` builds an executable `target/parque.jar` with jBCrypt, update `README.md`, add `input_fase3.txt`; verify by running it with `java -jar target/parque.jar --fly input_fase3.txt` and `mvn package` passing all tests
