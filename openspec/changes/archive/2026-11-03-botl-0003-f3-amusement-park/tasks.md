# Tasks

## 1. F3-C01 — Layered packages and exceptions

- [x] 1.1 Move classes into `presentation`, `business` and `persistence` packages (App stays as root composition class), introduce the `ParkRepository` interface implemented by `TextParkRepository`; verify `mvn test` passes with the migrated Delivery 2 suite
- [x] 1.2 Add the `ParkException` hierarchy (`ValidationException`, `NotFoundException`, `OperationNotAllowedException`, `PersistenceException`) and catch `ParkException` in `CommandProcessor`; verify a test that a failing command followed by a valid one executes both with a controlled error, and a test that presentation/business sources contain no file I/O

## 2. F3-C02 — Invoices with Builder

- [x] 2.1 Add `Invoice`, `InvoiceBuilder`, `AbstractInvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and `InvoiceController`; wire `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`; verify tests for user invoices (direct tickets only, empty sections omitted, unknown user), group invoices (group tickets with composition, total counted once, unknown group, invalid type), and builder unit tests (detail lines not counted)

## 3. F3-C03 — Singleton controllers and injection

- [x] 3.1 Turn services into singleton controllers with `configure(...)` and add `App.configure(ParkRepository, LocalDate)` as the wiring point used by `main` and tests; verify tests that each controller returns the same instance, has no public constructor, and that an injected failing repository is used without code changes

## 4. F3-C04 — Extended help

- [x] 4.1 Add `CommandInfo`, `HelpFormatter`, `SyntaxHelpFormatter`, `ExtendedHelpFormatter`, `HelpGenerator`, purposes for every command and `HELP EXTENDED`; verify tests that `HELP` lists syntax only and `HELP EXTENDED` lists every syntax with a non-empty purpose

## 5. F3-C05 — BCrypt passwords

- [x] 5.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin`, `PasswordHasher`, hash on create and PASSWORD update, verify on DELETE/UPDATE, legacy plaintext fails verification gracefully; verify tests: file has no plaintext and the hash verifies, old password rejected after change (also after restart), legacy record handled, and `java -jar` still starts

## 6. F3-C06 — SpeedPasses and Special Passes

- [x] 6.1 Add `Pass`, `SpeedPass`, `SpecialPass`, pass storage in `Park` and `PassController` with `SPEEDPASS CREATE`, `SPECIALPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS DELETE`; verify tests for prices (incl. discounted composed tickets), 1/10 quantities and ids, invalid quantity, past/group ticket, id conflict atomicity, one Special Pass per ticket, refund rules (consumed, today, wrong type, unknown)
- [x] 6.2 Cascade pass deletion in `TICKET DELETE` and `GROUPTICKET DELETE` with rollback; verify tests for both cascades and for failed-save rollback

## 7. F3-C07 — Generic attraction types

- [x] 7.1 Add `GeneralAccess`, `IndividualAccess`, `GroupAccess` implemented by the tickets, generic `Attraction<T extends GeneralAccess>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`, `AttractionType`, and the 4-parameter `ATTRACTION CREATE` (3-parameter → GENERAL); verify tests for typed creation, unknown type, accepted/rejected ticket kinds per type, and reflection checks of the generic bounds

## 8. F3-C08 — Pass consumption on entry

- [x] 8.1 Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` and automatic Special Pass use, consuming passes only after all checks, committing and undoing on failure; verify tests for individual SPEED entry (one pass consumed and reported), missing SpeedPass, atomic group SPEED entry (one member without pass → nothing changes; all with passes → one each), SPECIAL entry consumes, SPECIAL rejects SPEED, missing/consumed Special Pass, group ticket at SPECIAL, invalid fourth parameter

## 9. F3-C09 — Persistence and UML

- [x] 9.1 Add `PassRepository` (`passes.txt`) and attraction type field (legacy 3-field lines as GENERAL) in `TextParkRepository`; verify restart tests for passes (ticket, price, consumed) and types, malformed records skipped, and `--fly` runs leaving no files
- [x] 9.2 Rewrite `uml/model.puml` for the Delivery 3 design and update `UmlModelTest` for packages; verify PlantUML `-syntax` and the consistency test

## 10. F3-C10 — Delivery 3 integration

- [x] 10.1 Verify with a test that `HELP` lists all Delivery 1–3 syntaxes in order and `HELP EXTENDED` covers each
- [x] 10.2 Run `mvn clean package`, then the JAR with a mixed Delivery 1–3 command file normally, with `--fly <file>` and `--fly` interactively; verify outputs, persisted files (hashes, passes, types) and no leftover temporary data
- [x] 10.3 Verify the only runtime dependency is jBCrypt and application code uses no regex or Java serialization
