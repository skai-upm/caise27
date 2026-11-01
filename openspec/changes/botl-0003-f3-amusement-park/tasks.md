# Tasks

Milestones follow the source order. Each group ends with `mvn test` passing and its mandatory commit (exact message) — never before the tests pass.

## 1. F3-C01 Layered packages

- [x] 1.1 Move classes into `presentation`, `business` (`model`, `controller`-to-be, rules) and `persistence` packages and tests alongside; verify no class remains in the base/default package and all E1/E2 tests pass
- [x] 1.2 Verify `mvn package` builds `target/parque.jar` and commit `[F3-C01] Reorganize application into layered packages.`

## 2. F3-C02 Invoices with Builder

- [x] 2.1 Add `Invoice`, `InvoiceLine`, `InvoiceSection`, `InvoiceBuilder` and `StandardInvoiceBuilder` (empty sections skipped, total computed); verify with unit tests
- [x] 2.2 Add `INVOICE CREATE;USER;<correo>` / `INVOICE CREATE;GROUP;<id>` (direct tickets for users, group tickets for groups, no constituents) with text formatting; verify with command tests including unknown recipients and zero totals, then commit `[F3-C02] Add invoice generation with builder.`

## 3. F3-C03 Singleton controllers and DI

- [x] 3.1 Introduce repository interfaces in `business.repository` and `File*Repository` implementations in `persistence`; verify persistence tests pass against the implementations
- [x] 3.2 Split controllers into Singleton `SystemController`, `UserController`, `ParkController`, `InvoiceController` with `initialize`/`getInstance`/`release`, wired in `App`; verify with tests for same-instance access, double-initialisation rejection, an in-memory repository injected, and unchanged E1/E2 behaviour; commit `[F3-C03] Add singleton controllers and dependency injection.`

## 4. F3-C04 Extended help

- [x] 4.1 Add `CommandCatalog`, `HelpGenerator` with injected `HelpFormatter` (concise, extended) and `HELP EXTENDED`; verify with tests that both list every registered command, extended shows descriptions, and a custom formatter is honoured; commit `[F3-C04] Add extended help generation.`

## 5. F3-C05 BCrypt passwords

- [ ] 5.1 Add `jbcrypt` and the shade plugin, `PasswordHasher`/`BCryptPasswordHasher` injected into `UserController`; hash on create and password update after format validation; verify via tests that files hold no plaintext, wrong/correct passwords behave, new hash persists after restart, and legacy plaintext still authenticates unchanged; commit `[F3-C05] Secure stored user passwords.`

## 6. F3-C06 Passes

- [ ] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass` owned by `IndividualTicket`; add `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` with pricing, identifiers, lifetime Special Pass rule and E2 date rules; verify with tests for packs of 1/10, invalid quantities, constituent tickets, consumed/unrefundable rejections
- [ ] 6.2 Add the passes section to user invoices; verify that constituent passes appear while constituent tickets are not double-counted; commit `[F3-C06] Add speed and special passes.`

## 7. F3-C07 Attraction types

- [ ] 7.1 Add `Admission`/`IndividualAdmission`/`GroupAdmission` interfaces, generic `Attraction<T extends Admission>` with typed subclasses and `AttractionType`, optional type in `ATTRACTION CREATE`, constituent tickets usable for ENTER/EXIT; verify with tests for every type × ticket kind, E2 syntax → GENERAL, unknown type rejection; commit `[F3-C07] Add generic attraction access types.`

## 8. F3-C08 Pass-based entry

- [ ] 8.1 Add `;SPEED` to `ATTRACTION ENTER`, SpeedPass consumption (individual and atomic group), automatic Special Pass consumption and SPEED rejection at SPECIAL, consumed ids in output, persistence-safe undo; verify with tests for consumption counts, failed entries consuming nothing, group atomicity; commit `[F3-C08] Extend attraction access and pass consumption.`

## 9. F3-C09 Persistence and UML

- [ ] 9.1 Persist passes (with consumed state and special-pass history) and attraction types with backward-compatible loading and atomic file replacement; cascade deletions; verify with restart round-trip tests, cascade tests, failed-save no-partial-change tests, and fly isolation of F3 data
- [ ] 9.2 Regenerate `uml/model.puml` from the F3 code; verify PlantUML syntax check and rendering succeed and elements match the code; commit `[F3-C09] Extend delivery 3 persistence and UML.`

## 10. F3-C10 Integration

- [ ] 10.1 Integration check: `mvn package`, run `target/parque.jar` with an end-to-end command file mixing E1/E2/F3 commands interactively, from file, and with `--fly` (outputs equal, permanent files untouched, restart restores F3 state); verify `HELP`/`HELP EXTENDED` list all commands; commit `[F3-C10] Complete delivery 3 integration`
