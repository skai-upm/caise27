# Tasks

## 1. F3-C01 Layered packages and exceptions

- [x] 1.1 Move classes into `presentation`, `business.*` and `persistence` (rename `Park` → `business.controller.ParkController`), rewrite packages/imports and move tests alongside; verify the full existing suite passes unchanged in behavior
- [x] 1.2 Replace `OperationException` with the `ParkException` hierarchy (`InvalidDataException`, `NotFoundException`, `RuleViolationException`, `StorageException`) and presentation `InvalidCommandException`; verify tests asserting rejections and a test that processing continues after each exception type

## 2. F3-C03 Singleton controllers and injected repositories

- [x] 2.1 Introduce repository interfaces in `business.repository` and `File*Repository` implementations plus `FileRepositories.in(dir)`; replace `load(DataPaths)` with `ParkController.configure(Repositories)` wired by `App`; verify singleton identity tests and a test injecting a failing repository stub

## 3. F3-C04 Extended help

- [x] 3.1 Implement `CommandCatalog`/`CommandSpec`, `HelpGenerator` with injected `HelpStrategy` (`SyntaxHelp`, `ExtendedHelp`) and catalog-based command matching (keyword + arity variants); add `HELP EXTENDED`; verify help tests (all syntax lines, descriptions only in extended, custom strategy)

## 4. F3-C05 BCrypt passwords

- [x] 4.1 Add `org.mindrot:jbcrypt:0.4` and the shade plugin; implement `Passwords` and hash-on-register / hash-on-update / verify-on-authenticate in `UserManager`; verify the users file lacks the plaintext, BCrypt verification succeeds, old password rejected after change, and `java -jar` still starts

## 5. F3-C02 Invoices with Builder

- [x] 5.1 Implement `Invoice`, `InvoiceSection`, `InvoiceLine`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder` and singleton `InvoiceController` (director, injected with `ParkController`); add `INVOICE CREATE;USER|GROUP`; verify user/group invoice contents, totals, omitted sections and unknown recipients (pass sections verified after group 6)

## 6. F3-C06 Speed and Special Passes

- [x] 6.1 Implement `Pass`, `SpeedPass`, `SpecialPass` with stored prices; add `SPEEDPASS CREATE` (1 or 10, `<id>:<n>`, atomic id check, total shown) and `SPECIALPASS CREATE` (one per ticket, price shown); verify pricing (incl. discounted composition ticket and later SYSTEM PRICE), identifiers and all rejection cases
- [x] 6.2 Add `SPEEDPASS DELETE` / `SPECIALPASS DELETE` refunds (unconsumed, ticket strictly future) and cascade deletion of passes in `TICKET DELETE` / `GROUPTICKET DELETE`; verify refunds, rejections and cascades leave no partial state

## 7. F3-C07 Generic attraction types

- [x] 7.1 Add access interfaces, generic `Attraction<T extends AccessTicket>` with General/Individual/Group/Special subclasses and `AttractionType`; extend `ATTRACTION CREATE` with optional type (default GENERAL); verify accepted/rejected ticket types per attraction type and all Delivery 2 entry tests

## 8. F3-C08 Atomic pass consumption

- [x] 8.1 Add `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` and automatic Special Pass consumption via `selectPasses`, consuming only on success and persisting consumption with rollback; verify individual SPEED entry, group SPEED atomicity (one member lacking a pass), SPECIAL entry, SPEED rejected at SPECIAL, consumed Special Pass rejected, and output naming consumed passes

## 9. F3-C09 Persistence and UML

- [x] 9.1 Implement `FilePassRepository` and attraction type persistence (legacy 3-field lines → GENERAL); verify restart restores passes (ticket, price, consumed) and types, and `--fly` covers passes
- [x] 9.2 Rewrite `uml/model.puml` for the Delivery 3 design; verify `-checkonly` and Smetana render with the cached PlantUML jar and review each element against the code

## 10. F3-C10 Delivery 3 integration

- [x] 10.1 Run `mvn clean package`; run the shaded JAR with a mixed Delivery 1–3 command file in normal mode, after restart, and with `--fly` (file and interactive); verify outputs, data files (no plaintext password, passes persisted) and no remaining `tmp/` data
- [x] 10.2 Verify restrictions: runtime dependencies are only jBCrypt, no serialization/regex APIs in `src/main/java`, presentation imports no persistence/file APIs, business imports no persistence package
