# Tasks

## 1. [F3-C01] Layered packages

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller` (temporarily `Park`), `business.validation` and `persistence`, move tests to matching packages and update the manifest main class; verify `mvn test` passes and `mvn package` produces a runnable JAR

## 2. [F3-C03] Singleton controllers and dependency injection

- [x] 2.1 Introduce repository interfaces with `File*` implementations; verify existing repository tests pass against the file implementations
- [x] 2.2 Split `Park` into singleton `ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController` with `configure`/`load`, and add `Bootstrap` that creates and injects repositories and collaborators for normal and `--fly` data dirs; verify a test that `getInstance()` returns the same object, that bootstrap re-initialisation resets state, and that all E1/E2 scenario tests still pass

## 3. [F3-C05] Hashed passwords

- [x] 3.1 Add `org.mindrot:jbcrypt` and `maven-shade-plugin` to `pom.xml`, add `PasswordHasher` and use it in `UserController` for create, authenticate, password update and legacy migration; verify tests that the stored value is a verifying hash, wrong passwords are rejected, password change persists a new hash, invalid formats are rejected, and a plain-text users file is migrated on load

## 4. [F3-C04] Extended help

- [x] 4.1 Add `CommandInfo`, `CommandCatalog`, `HelpFormat` with `SyntaxHelpFormat`/`ExtendedHelpFormat`, and `HelpGenerator` injected into `CommandProcessor`; handle `HELP` and `HELP EXTENDED`; verify tests that HELP lists every catalog syntax, HELP EXTENDED adds descriptions, and a custom injected format is used

## 5. [F3-C06] Personal passes

- [x] 5.1 Add `Pass`, `SpeedPass`, `SpecialPass` composed into `IndividualTicket`; verify unit tests for pass prices (20% / 100%) and id generation
- [x] 5.2 Implement `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` in `TicketController`/`CommandProcessor` with amount replies; verify tests for quantities 1/10/invalid, group-generated tickets, past tickets, the single Special Pass rule, refunds of unused future passes, and rejection for consumed or non-future passes

## 6. [F3-C07] Typed attractions with generics

- [x] 6.1 Add `Admission`, `AttractionType` and generic `Attraction<T extends Ticket>` with GENERAL/INDIVIDUAL/GROUP/SPECIAL subclasses; accept optional type in `ATTRACTION CREATE`; verify tests that each type admits/rejects the right ticket kinds and that E2 syntax creates GENERAL

## 7. [F3-C08] Pass consumption on entry

- [x] 7.1 Implement `SPEED` and Special Pass handling in `AttractionController.enter` with atomic checks before entry and consumption after; verify tests for single and group SPEED entry, atomic failure without consumption, SPECIAL consumption and reuse rejection, SPEED rejected at SPECIAL, and reported consumption

## 8. [F3-C02] Invoices with Builder

- [x] 8.1 Add `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `StandardInvoiceBuilder` and `InvoiceController` (director) with `INVOICE CREATE;USER|GROUP;…`; verify tests for the user invoice example (88.00), group invoice, empty invoice, unknown recipients, and that group-generated tickets are not counted as individual purchases

## 9. [F3-C09] Persistence and UML

- [x] 9.1 Add `FilePassRepository` (`passes.txt`) and the attraction type column, wire them in `Bootstrap`, and make ticket/group-ticket deletion cascade to passes without partial deletions; verify restart tests for passes (price, consumed) and attraction type, cascade tests, a rejected deletion test, and a `--fly` test with passes
- [x] 9.2 Rewrite `uml/model.puml` for the E3 code following the E2 conventions; verify it passes the PlantUML syntax check and renders, and matches the classes, interfaces, generics and fields in the source

## 10. [F3-C10] Integration

- [x] 10.1 Run `mvn test` and `mvn package`, then run the JAR with `--fly` on a command file exercising E1, E2 and E3 commands (including HELP EXTENDED, invoices, passes and typed attractions) and confirm results and that no normal data files are touched
