# Tasks

## 1. F3-C01 Layered packages

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller`, `business.repository` and `persistence` (rename `*Manager`→`*Controller`, `*Storage`→`*FileRepository` implementing new repository interfaces), move tests to matching packages and update the JAR main class; verify no class remains in `es.upm.amusementpark` root, presentation uses `java.nio.file` only to read the command-input file (no data-file access), and no `business` class imports `persistence`
- [x] 1.2 Verify `mvn package` passes all existing tests and a sample E1/E2 command file yields the same output as before the move; commit `[F3-C01] Reorganize application into layered packages.`

## 2. F3-C02 Invoices with Builder

- [x] 2.1 Add `Invoice`, `InvoiceLine`, `InvoiceBuilder`, `TextInvoiceBuilder` and `InvoiceController` (user: directly bought individual tickets; group: group tickets) plus `INVOICE CREATE;USER|GROUP;…`; verify tests for user/group totals, constituent tickets not listed on user invoices, empty sections omitted, unknown recipient rejected; commit `[F3-C02] Add invoice generation with builder.`

## 3. F3-C03 Singleton controllers and DI

- [x] 3.1 Convert controllers to Singletons (`initialize`/`getInstance`/`reset`), add `AppBootstrap.start/shutdown` creating repositories for the chosen `StoragePaths` and injecting them and controller collaborators; verify tests that `getInstance` returns the same object, double initialization fails, an in-memory repository can be injected, and normal/`--fly` runs still pass; commit `[F3-C03] Add singleton controllers and dependency injection.`

## 4. F3-C04 Extended help

- [x] 4.1 Add `CommandRegistry`/`CommandInfo`, `HelpFormatter` with concise and extended implementations, and `HelpGenerator` injected into `CommandProcessor`; implement `HELP EXTENDED`; verify tests that HELP shows syntax, HELP EXTENDED shows syntax plus descriptions for every registered command, and a custom formatter can be injected; commit `[F3-C04] Add extended help generation.`

## 5. F3-C05 Password security

- [x] 5.1 Add `jbcrypt` dependency and shade plugin, `PasswordHasher` + `BCryptPasswordHasher`, hash on create/password change, verify via hash (legacy plaintext fallback); verify tests that files contain no plaintext, correct/incorrect passwords on delete/update, changed password authenticates after restart, E1 format rules still enforced, and `java -jar target/parque.jar` runs; commit `[F3-C05] Secure stored user passwords.`

## 6. F3-C06 Personal passes

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, ticket ownership, `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` in `TicketController`/`ParkController`, pass persistence lines in the ticket repository, and the personal-passes invoice section; verify tests for quantity 1/10 and invalid quantity, prices (20% / 100%), one Special Pass per lifetime, refunds only for unused passes of future tickets, persistence round trip of prices/states, and invoice inclusion of passes on group-owned tickets; commit `[F3-C06] Add speed and special passes.`

## 7. F3-C07 Typed attractions with generics

- [x] 7.1 Add `AttractionAccess`, `IndividualAccess`, `GroupAccess`, `AttractionType`, generic `Attraction<T extends AttractionAccess>` with `General/Individual/Group/SpecialAttraction`, and the optional type parameter in `ATTRACTION CREATE`; verify tests for default GENERAL, each type accepting/rejecting individual and group tickets, and unknown type rejected; commit `[F3-C07] Add generic attraction access types.`

## 8. F3-C08 Pass-based entry

- [x] 8.1 Add the optional `SPEED` argument and pass preflight/consumption to entry, Special Pass consumption at SPECIAL attractions, persistence of consumed state and reply reporting consumed passes; verify tests for single SpeedPass consumption, group SPEED atomicity (one member without pass → nobody enters, nothing consumed), SPECIAL consumption and SPEED rejection, failed entries consuming nothing; commit `[F3-C08] Extend attraction access and pass consumption.`

## 9. F3-C09 Persistence and UML

- [x] 9.1 Persist attraction type (3-column lines as GENERAL), write repositories atomically, and verify cascade deletion of passes for `TICKET DELETE` and `GROUPTICKET DELETE` (passes → constituents → group ticket) with no partial state on failure; verify tests for type round trip, cascade after restart, simulated write failure leaving memory and file unchanged, and `--fly` leaving permanent files untouched with F3 data
- [x] 9.2 Update `uml/model.puml` from the final code (packages, passes, invoice builder, access interfaces, generic attractions with bounds, `AttractionType`); verify it renders with PlantUML and matches the fields in code; commit `[F3-C09] Extend delivery 3 persistence and UML.`

## 10. F3-C10 Integration

- [x] 10.1 Ensure `HELP`/`HELP EXTENDED` list every command; run an end-to-end E1+E2+F3 command file via `java -jar target/parque.jar` interactively, from file and with `--fly`, checking identical replies, restart reconstruction and untouched permanent files; verify `mvn package` and `openspec validate` succeed; commit `[F3-C10] Complete delivery 3 integration`
