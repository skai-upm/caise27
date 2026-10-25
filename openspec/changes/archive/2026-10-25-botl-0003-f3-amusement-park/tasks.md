# Tasks

## 1. Layered packages [F3-C01]

- [x] 1.1 Move domain classes to `business.model`, validators to `business.validation`, file repositories to `persistence` (as `File*Repository` implementing interfaces in `business.repository`), command handling to `presentation`, keeping `App` in `es.upm.parque`; move tests to matching packages; verify `mvn test` passes with no class in the default package
- [x] 1.2 Verify presentation and business packages contain no file I/O (grep for `java.nio.file.Files` outside persistence/bootstrap) and that all E1/E2 command tests still pass

## 2. Singleton controllers and dependency injection [F3-C03]

- [x] 2.1 Turn `ParkController` and the managers into singleton controllers (`ParkController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`) with `configure(...)` (repositories + collaborators) and `load()`; verify a unit test that `getInstance()` returns the same object
- [x] 2.2 Add `ParkInitializer` creating repositories for a data directory, configuring and loading all controllers in order, and building the `CommandProcessor`; verify tests that normal and `--fly` runs use different directories with the same controllers

## 3. Command registry and extended help [F3-C04]

- [x] 3.1 Introduce `Command` (min/max params, description, handler returning text), `CommandRegistry` and per-area command sets; verify optional-parameter tests (omitted, too many)
- [x] 3.2 Implement `HelpGenerator` with injected `HelpFormat` strategies (`SyntaxHelpFormat`, `ExtendedHelpFormat`) and the `HELP` / `HELP EXTENDED` commands; verify tests that both list every registered command and that extended help includes descriptions

## 4. Password protection [F3-C05]

- [x] 4.1 Add `org.mindrot:jbcrypt` to `pom.xml` and `maven-shade-plugin` producing executable `target/parque.jar`; verify `mvn package` and `java -jar target/parque.jar` with a `USER CREATE` command
- [x] 4.2 Add `PasswordHasher`/`BCryptPasswordHasher`; store hashes, authenticate with `matches`, re-hash on password update, validate plaintext before hashing; verify tests for authentication, password change and that `users.txt` never contains plaintext
- [x] 4.3 Migrate legacy plaintext passwords on load; verify a test loading a Delivery 2 style `users.txt`

## 5. Personal passes [F3-C06]

- [x] 5.1 Implement `Pass`, `SpeedPass`, `SpecialPass` owned by `IndividualTicket`, and `PassController` with `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` (quantities 1/10, 20%/100% pricing, one Special Pass, refund rules); verify tests for every spec scenario
- [x] 5.2 Implement `FilePassRepository` (`passes.txt`) restoring passes onto tickets with price and consumption; verify restart test

## 6. Attraction types and pass consumption [F3-C07, F3-C08]

- [x] 6.1 Add `Admission` interface implemented by tickets and generic `Attraction<T extends Admission>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`; support the optional type in `ATTRACTION CREATE`; verify the type/ticket-kind matrix test
- [x] 6.2 Extend `ATTRACTION ENTER` with `SPEED` (one SpeedPass per access ticket, atomic) and Special Pass consumption at SPECIAL attractions, reporting consumed passes; verify tests for individual/group SPEED, atomic failure, failed entry consuming nothing, special pass missing/used, SPEED rejected at SPECIAL

## 7. Persistence, cascades and UML [F3-C09]

- [x] 7.1 Persist attraction type (legacy lines as GENERAL) and save passes after consumption; verify restart tests
- [x] 7.2 Cascade deletions: `TICKET DELETE` removes passes; `GROUPTICKET DELETE` removes member passes, then internal tickets, then the group ticket; failed deletions change nothing; verify tests including restart and `--fly` coverage of `passes.txt`
- [x] 7.3 Rewrite `uml/model.puml` for the E3 design; verify with PlantUML `-checkonly` and a manual comparison with the code

## 8. Invoices [F3-C02]

- [x] 8.1 Implement `InvoiceBuilder`, `StandardInvoiceBuilder`, `InvoiceDirector`, `Invoice` model and `InvoiceController` with injected builder supplier, plus `INVOICE CREATE;USER|GROUP;…`; verify tests for user invoice (direct tickets + passes, internal tickets not counted), group invoice, empty user, unknown recipients, and that no file changes

## 9. Integration [F3-C10]

- [x] 9.1 Run `mvn test` and `mvn package`; run an end-to-end command file covering E1, E2 and E3 commands in normal and `--fly` modes via `java -jar target/parque.jar`; verify HELP/HELP EXTENDED list all commands and `tmp/` is empty afterwards
