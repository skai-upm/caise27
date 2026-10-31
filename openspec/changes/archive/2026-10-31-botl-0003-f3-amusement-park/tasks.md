# Tasks

Each group ends with `mvn test` passing and a local commit with the exact message shown.

## 1. F3-C01 Layered packages

- [x] 1.1 Move sources and tests into `presentation`, `business.{model,controller,rules}` and `persistence` packages without behavior changes; verify no class remains in `es.upm.amusementpark` root and the full E1/E2 test suite passes
- [x] 1.2 Move `--fly` directory creation/deletion from `App` to `persistence.TemporaryStorage`; verify `FlyModeTest` passes and `mvn package` builds the JAR
- [x] 1.3 Commit `[F3-C01] Reorganize application into layered packages.`

## 2. F3-C02 Invoices

- [x] 2.1 Add `business.invoice` (`Invoice`, `InvoiceBuilder`, `InvoiceService`) and `INVOICE CREATE;USER|GROUP;…`; verify tests for user invoice (direct tickets only, constituents excluded), group invoice totals, omitted empty sections, unknown recipient/type
- [x] 2.2 Commit `[F3-C02] Add invoice generation with builder.`

## 3. F3-C03 Singletons and injection

- [x] 3.1 Introduce repository interfaces in `business.repository` with `File*Repository` implementations; make controllers Singletons with `initialize/getInstance/reset`; add `ApplicationContext` wiring for normal and `--fly`; verify tests for same-instance access, double initialisation rejected, in-memory repository injection, and the full regression suite
- [x] 3.2 Commit `[F3-C03] Add singleton controllers and dependency injection.`

## 4. F3-C04 Extended help

- [x] 4.1 Add `CommandRegistry`, `HelpGenerator`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, inject them into `CommandProcessor`, add `HELP EXTENDED`; verify tests that HELP shows syntax, HELP EXTENDED shows descriptions for every registered command, and a custom injected formatter is used
- [x] 4.2 Commit `[F3-C04] Add extended help generation.`

## 5. F3-C05 Password security

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4`, `PasswordHasher`/`BCryptPasswordHasher`, hash on create and password update, verify on delete/update, legacy plaintext fallback, shade plugin for the executable JAR; verify tests for no plaintext in `users.txt`, correct/incorrect password, new hash after change and restart, legacy user login, E1 rules before hashing
- [x] 5.2 Commit `[F3-C05] Secure stored user passwords.`

## 6. F3-C06 Personal passes

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, ticket composition, `SPEEDPASS/SPECIALPASS CREATE/DELETE`, `passes.txt` persistence and the personal-pass invoice section; verify tests for quantities 1/10/other, 20% and full price, lifetime Special Pass limit, refund rules, consumed passes not refundable, restart restoration, invoice with passes on constituent tickets
- [x] 6.2 Commit `[F3-C06] Add speed and special passes.`

## 7. F3-C07 Typed attractions

- [x] 7.1 Add `AccessTicket`/`IndividualAccess`/`GroupAccess`, generic `Attraction<T extends AccessTicket>` with four subclasses and `AttractionType`, optional type in `ATTRACTION CREATE`, type persisted with legacy default; verify tests for each type × ticket kind, E2 syntax creating GENERAL, invalid type
- [x] 7.2 Commit `[F3-C07] Add generic attraction access types.`

## 8. F3-C08 Pass-based entry

- [x] 8.1 Add `;SPEED` handling and Special Pass consumption with full preflight; verify tests for individual SPEED consuming one pass and reporting it, group SPEED consuming one per member, atomic group failure, failed entries consuming nothing, SPECIAL auto-consumption and SPEED rejection
- [x] 8.2 Commit `[F3-C08] Extend attraction access and pass consumption.`

## 9. F3-C09 Persistence and UML

- [x] 9.1 Add staged multi-file writes in `TextFileStore` and use them for the ticket aggregate; verify tests for cascade deletion of passes with tickets and group tickets across restart, failed save leaving files and memory unchanged, `--fly` isolating `passes.txt`
- [x] 9.2 Update `uml/model.puml` to the F3 code (packages, interfaces, generics, passes, invoice, repositories); verify PlantUML `-checkonly` and a Smetana render succeed and relationships match declared fields
- [x] 9.3 Commit `[F3-C09] Extend delivery 3 persistence and UML.`

## 10. F3-C10 Integration

- [x] 10.1 Run `mvn test` and `mvn package`; run the shaded JAR with a mixed E1/E2/F3 command file normally, interactively and with `--fly`, checking HELP/HELP EXTENDED, invoices, passes, typed entry and restart restoration
- [x] 10.2 Commit `[F3-C10] Complete delivery 3 integration`
