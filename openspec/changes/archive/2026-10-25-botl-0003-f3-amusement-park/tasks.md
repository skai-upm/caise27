# Tasks

## 1. [F3-C01] Layered packages

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller`, `business.validation` and `persistence` packages (entry point stays `es.upm.parque.App`), moving tests accordingly; verify `mvn package` succeeds, no class is in the default package and all E1/E2 tests pass

## 2. [F3-C03] Singleton controllers and dependency injection

- [x] 2.1 Introduce repository interfaces with file implementations, split `ParkController` into Singleton controllers (`ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`) configured and loaded by `ParkApplication` at startup with injected repositories and collaborators; verify with tests for single instances, reconfiguration with different paths and unchanged E1/E2 behavior

## 3. [F3-C04] Extended help

- [x] 3.1 Replace the switch-based processor with `Command`/`CommandRegistry`/`ParkCommands`, add `HelpGenerator` with injected `SyntaxHelpFormatter`/`ExtendedHelpFormatter` and the `HELP EXTENDED` command; verify with tests that both helps list every registered command and that a newly registered command appears automatically

## 4. [F3-C05] Password security

- [x] 4.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin to `pom.xml`, add `PasswordEncoder`/`BCryptPasswordEncoder`, store hashes, verify credentials against them and migrate plaintext on load; verify with tests for creation, delete/update authentication, password change and legacy migration

## 5. [F3-C06] Personal passes

- [x] 5.1 Add `Pass`, `SpeedPass`, `SpecialPass` composed by `IndividualTicket` (with the lifetime Special Pass flag) and `PassController` with `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` (quantities 1/10, prices, refund rules); verify with unit tests for prices, quantities, the one-Special-Pass rule and refunds

## 6. [F3-C07] Typed attractions

- [x] 6.1 Add `AccessCapability`, `IndividualAccess`, `GroupAccess`, generic `Attraction<T extends AccessCapability>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType` and the optional type in `ATTRACTION CREATE`; verify with tests that GROUP rejects individual tickets, INDIVIDUAL rejects group tickets and the E2 syntax creates GENERAL

## 7. [F3-C08] Pass-aware entry

- [x] 7.1 Implement `ATTRACTION ENTER;…;SPEED` and automatic Special Pass consumption with atomic checks and consumption reporting; verify with tests for individual/group SPEED, missing passes, capacity failures without consumption, SPECIAL entry and SPEED rejection at SPECIAL

## 8. [F3-C02] Invoices

- [x] 8.1 Add `Invoice`, `InvoiceBuilder`, `StandardInvoiceBuilder`, `InvoiceDirector` and `InvoiceController` with `INVOICE CREATE;USER|GROUP;…`; verify with tests for user invoices (direct tickets plus all passes, no internal tickets), group invoices (group tickets only), omitted empty sections and totals

## 9. [F3-C09] Persistence and UML

- [x] 9.1 Persist passes, the Special Pass flag and attraction types (backward-compatible loading), add `StoragePaths.passes()`, and cascade ticket/group-ticket deletion to passes without partial deletions; verify with restart tests and cascade tests
- [x] 9.2 Rewrite `uml/model.puml` for the E3 design; verify with PlantUML `-checkonly` and a cross-check against the code

## 10. [F3-C10] Integration

- [x] 10.1 Update README (packages, new commands, BCrypt), then run `mvn package`, `java -jar target/parque.jar --fly` with an E1+E2+E3 command file, and verify HELP/HELP EXTENDED, outputs and that `data/` is untouched
