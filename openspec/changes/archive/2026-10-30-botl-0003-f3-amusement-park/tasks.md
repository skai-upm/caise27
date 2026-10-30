# Tasks

Each milestone ends with `mvn test` green and the mandated commit (exact message). Earlier behavior must keep working.

## 1. F3-C01 Layered packages — commit `[F3-C01] Reorganize application into layered packages.`

- [x] 1.1 Move classes to `presentation`, `business.model`, `business.controller`, `business.validation`, `persistence`; extract `Formats` (date/price) into `business.model`; move tests to mirrored packages; verify `mvn package` builds and all 126 existing tests pass
- [x] 1.2 Verify no class remains in the default/root package and no presentation/model class does file I/O on data files (grep), then create the F3-C01 commit

## 2. F3-C02 Invoices — commit `[F3-C02] Add invoice generation with builder.`

- [x] 2.1 Add `business.invoice` (`Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `TextInvoiceBuilder`) and an invoice director with user (direct tickets only) and group (group tickets) sections; verify builder unit tests (empty sections omitted, total computed)
- [x] 2.2 Add `INVOICE CREATE;USER;<correo>` and `INVOICE CREATE;GROUP;<id>`; verify tests (no double counting of constituents, group total 188.00 example, unknown user/group/type rejected), then commit F3-C02

## 3. F3-C03 Singleton controllers and DI — commit `[F3-C03] Add singleton controllers and dependency injection.`

- [x] 3.1 Introduce repository interfaces with `File*Repository` implementations; rename `Park`/services to `ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` with Singleton `getInstance()` and `configure(...)` injection
- [x] 3.2 Add `ApplicationBootstrap` creating repositories from `StorageLocation` and wiring controllers; verify tests for same-instance Singletons, normal vs `--fly` wiring, and all previous tests; then commit F3-C03

## 4. F3-C04 Extended help — commit `[F3-C04] Add extended help generation.`

- [x] 4.1 Add `CommandRegistry`/`CommandDefinition` and dispatch through it; add `HelpFormatter` (concise/extended), `HelpGenerator` with injected formatters, and `HELP EXTENDED`; verify tests (both outputs list every command, extended has descriptions, injected custom formatter used), then commit F3-C04

## 5. F3-C05 Passwords — commit `[F3-C05] Secure stored user passwords.`

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4` and shade plugin; add `PasswordHasher`/`BCryptPasswordHasher` injected into `UserController`; store hashes, verify on delete/update, rehash on change, accept legacy plaintext; verify tests (no plaintext in file, wrong/right password, new hash after change and restart, legacy line, format rules before hashing) and that the shaded JAR runs, then commit F3-C05

## 6. F3-C06 Passes — commit `[F3-C06] Add speed and special passes.`

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass` owned by `IndividualTicket`; `PassController` with `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` (quantities 1/10, 20 % price, one Special Pass, refund rules); verify tests for prices, ids, quantities, one-special rule (also after consumption), refund rules
- [x] 6.2 Add the personal-pass section to user invoices (including passes on group constituents) and verify invoice tests, then commit F3-C06

## 7. F3-C07 Attraction types — commit `[F3-C07] Add generic attraction access types.`

- [x] 7.1 Add `AccessTicket`, `IndividualAccess`, `GroupAccess`; make tickets implement them; turn `Attraction` into `Attraction<T extends AccessTicket>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; extend `ATTRACTION CREATE` with optional type and `ATTRACTION STATUS` with type; verify kind acceptance/rejection tests and that the E2 syntax creates `GENERAL`, then commit F3-C07

## 8. F3-C08 Pass-based entry — commit `[F3-C08] Extend attraction access and pass consumption.`

- [x] 8.1 Add `;SPEED` option and pass requirements (`requiredPasses`) with atomic consumption and reporting; SPECIAL consumes Special Pass and rejects SPEED; verify tests (one pass consumed, group SPEED atomic, failed entry consumes nothing, special consumed/rejected), then commit F3-C08

## 9. F3-C09 Persistence and UML — commit `[F3-C09] Extend delivery 3 persistence and UML.`

- [x] 9.1 Persist passes (`passes.txt`) and attraction types (legacy lines → GENERAL); cascade deletion for `TICKET DELETE` and `GROUPTICKET DELETE` with safe writes and rollback; verify restart round-trip, cascade, failed deletion leaves no partial state, `--fly` isolation of new files
- [x] 9.2 Update `uml/model.puml` from the final code and verify it renders with PlantUML, then commit F3-C09

## 10. F3-C10 Integration — commit `[F3-C10] Complete delivery 3 integration`

- [x] 10.1 Verify HELP/HELP EXTENDED list all commands, update README, run `mvn clean package` and an end-to-end E1+E2+F3 command file with the JAR in normal mode (restart included), interactive mode and `--fly` (permanent files unchanged); then commit F3-C10
