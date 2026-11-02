# Tasks

## 1. F3-C01 Layered packages

- [x] 1.1 Move every class into `presentation`, `business.model`, `business.controller`, and `persistence` packages (tests mirrored), fix visibility and imports, update the manifest main class; verify no class remains in `com.amusementpark` root or the default package
- [x] 1.2 Verify `mvn test` passes with the E2 suite unchanged in behavior and `mvn package` produces `target/parque.jar`, then commit `[F3-C01] Reorganize application into layered packages.`

## 2. F3-C02 Invoices with Builder

- [x] 2.1 Add `Invoice`, `InvoiceBuilder`, `TextInvoiceBuilder` and the invoice director, plus `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<idGrupo>` with ticket and group-ticket sections; verify with tests for user/group invoices, omitted empty sections, unknown recipients, and no double counting of constituents
- [x] 2.2 Run `mvn test` and commit `[F3-C02] Add invoice generation with builder.`

## 3. F3-C03 Singleton controllers and dependency injection

- [x] 3.1 Rename managers to `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController`; make them and `ParkController` Singletons with `configure(...)`; introduce repository interfaces with `*FileRepository` implementations; wire everything in `App.configure`; verify with tests that `getInstance()` returns the same object and that normal and `--fly` runs use different repository paths
- [x] 3.2 Run `mvn test` and commit `[F3-C03] Add singleton controllers and dependency injection.`

## 4. F3-C04 Extended help

- [x] 4.1 Add the command registry (`CommandDefinition`), `HelpGenerator` with injected `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, and `HELP EXTENDED`; verify with tests that `HELP` shows syntax only, `HELP EXTENDED` shows syntax and descriptions for every registered command, and a test formatter can be injected
- [x] 4.2 Run `mvn test` and commit `[F3-C04] Add extended help generation.`

## 5. F3-C05 BCrypt passwords

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin, `PasswordHasher`/`BCryptPasswordHasher`, hashing on create and password update, verification on delete/update, legacy plaintext comparison (design D6); verify with tests that no plaintext is stored, correct/incorrect passwords behave, a changed password persists a new hash usable after restart, and E1 format rules still apply
- [x] 5.2 Run `mvn test`, check `java -jar target/parque.jar` still starts, and commit `[F3-C05] Secure stored user passwords.`

## 6. F3-C06 SpeedPass and Special Pass

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, ticket-owned passes, `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` per design D7, and the invoice passes section; verify with tests for quantities 1/10/invalid, prices, one Special Pass per lifetime, refund of unused passes only when the ticket is refundable, and invoices including constituent-ticket passes
- [x] 6.2 Run `mvn test` and commit `[F3-C06] Add speed and special passes.`

## 7. F3-C07 Typed attractions with interfaces and generics

- [x] 7.1 Add `Admission`, `IndividualAdmission`, `GroupAdmission`, generic `Attraction<T extends Admission>` with the four subclasses and `AttractionType`, optional type in `ATTRACTION CREATE`, and type in `ATTRACTION STATUS`; verify with tests for default GENERAL creation, unknown types, and accept/reject of each ticket kind per type
- [x] 7.2 Run `mvn test` and commit `[F3-C07] Add generic attraction access types.`

## 8. F3-C08 Pass-based entry

- [x] 8.1 Add the optional `SPEED` field to `ATTRACTION ENTER`, pass selection before any change, consumption with reported pass identifiers, atomic group SPEED entry, and SPECIAL attraction rules; verify with tests for single consumption, atomic group failure, one pass per member, automatic Special Pass consumption, and SPEED rejection at SPECIAL
- [x] 8.2 Run `mvn test` and commit `[F3-C08] Extend attraction access and pass consumption.`

## 9. F3-C09 Persistence and UML

- [x] 9.1 Persist passes (`passes.txt`), the special-pass flag, and attraction types with E2-compatible loading, cascade deletion with preflight and rollback, and `--fly` isolation of `passes.txt`; verify with restart, cascade, legacy-file, and fly tests
- [x] 9.2 Update `uml/model.puml` from the final code (layers, interfaces, generics, passes, invoice builder, repositories) and verify it with PlantUML `-checkonly`
- [x] 9.3 Run `mvn test` and commit `[F3-C09] Extend delivery 3 persistence and UML.`

## 10. F3-C10 Integration

- [x] 10.1 Verify `HELP` and `HELP EXTENDED` list every command, then run an end-to-end E1+E2+F3 command file with `target/parque.jar` in normal mode (twice, restart) and `--fly` mode and interactively, confirming equal outcomes and untouched permanent files after `--fly`
- [x] 10.2 Run `mvn test` and `mvn package`, validate the OpenSpec change, and commit `[F3-C10] Complete delivery 3 integration`
