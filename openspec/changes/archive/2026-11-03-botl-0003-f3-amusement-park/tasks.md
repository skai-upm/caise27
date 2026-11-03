# Tasks

## 1. F3-C01 — Layered packages and exceptions

- [x] 1.1 Move classes into `presentation`, `business` (+ `ticket`, `group`, `attraction`, `repository`, `controller`, `invoice`) and `persistence` packages with `App` at the root, and replace the single exception with the `ParkException` hierarchy (`InvalidDataException`, `NotFoundException`, `RuleViolationException`, `StorageException`); verify all Delivery 1/2 tests pass after moving them, and add a test asserting presentation classes do not import persistence

## 2. F3-C03 — Singleton controllers and injection

- [x] 2.1 Define repository interfaces in `business.repository` with the `ParkRepositories` bundle, rename implementations to `Text*Repository` with a `TextRepositories.inDirectory` factory, and inject them (plus the `PasswordHasher`) via `ParkController.initialize`; add `InvoiceController` singleton configured with `ParkController`; verify tests for singleton identity and for a controller running on an in-memory repository implementation

## 3. F3-C04 — Extended help

- [x] 3.1 Add `CommandCatalog`, `HelpGenerator`, `HelpFormat`, `SyntaxHelpFormat` and `ExtendedHelpFormat`, injected into `CommandProcessor` for `HELP` and `HELP EXTENDED`; verify tests for both outputs and for a custom injected format

## 4. F3-C05 — BCrypt passwords

- [x] 4.1 Add `org.mindrot:jbcrypt:0.4` and `maven-shade-plugin` to `pom.xml`, implement `BCryptPasswordHasher`, hash on create/password update and verify on delete/update; verify tests that the user file has no plaintext, old/new password behaviour after an update and restart, invalid password rejection, and legacy plaintext failing cleanly

## 5. F3-C06 — SpeedPasses and Special Passes

- [x] 5.1 Implement `Pass`, `SpeedPass`, `SpecialPass` and the controller operations `SPEEDPASS CREATE/DELETE`, `SPECIALPASS CREATE/DELETE` and cascading `TICKET DELETE`/`GROUPTICKET DELETE`; verify tests for 1/10 packs and prices (including discounted composed tickets), identifier conflicts, quantity/ticket/date rejections, one Special Pass per ticket, refund rules and cascades

## 6. F3-C07 — Generic attraction types

- [x] 6.1 Implement `AccessTicket`, `IndividualAccess`, `GroupAccess`, the `Attraction` interface, `AbstractAttraction<T>` and the four typed attractions with `AttractionType`, 4/5-argument `ATTRACTION CREATE` and the type in `ATTRACTION STATUS`; verify tests for accepted/rejected ticket kinds per type, legacy syntax as GENERAL and unknown types

## 7. F3-C08 — Entry and pass consumption

- [x] 7.1 Extend `ATTRACTION ENTER` with the optional `SPEED` argument, SpeedPass consumption per entering ticket, automatic Special Pass consumption at SPECIAL attractions, and commit/undo of consumption; verify tests for individual and group SPEED entry, a group member without a SpeedPass, SPECIAL with/without/consumed pass, SPEED at SPECIAL, invalid option and full attraction leaving passes untouched

## 8. F3-C02 — Invoice builders

- [x] 8.1 Implement `Invoice`, abstract `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceController` queries and `INVOICE CREATE;USER|GROUP;…`; verify tests for omitted empty sections, user totals excluding composed tickets but including their passes, group totals counted once without passes, and unknown user/group/type errors

## 9. F3-C09 — Persistence and UML

- [x] 9.1 Add `TextPassRepository` (`passes.txt`) and the attraction type column (3-column lines load as GENERAL), wiring passes into load/save; verify round-trip tests for pass ticket/price/consumed state, attraction types, legacy attraction lines and cascades after restart
- [x] 9.2 Rewrite `uml/model.puml` for the Delivery-3 design; verify it parses with the cached PlantUML jar and review it against the code

## 10. F3-C10 — Delivery 3 integration

- [x] 10.1 Ensure `HELP`/`HELP EXTENDED` list all Delivery 1–3 commands and add an end-to-end mixed Delivery 1–3 command-file test in file and `--fly` modes; verify the tests pass
- [x] 10.2 Run `mvn clean package` and run `target/nombre-aplicacion.jar` interactively, with `input_fase3.txt` and with `--fly input_fase3.txt` from a temporary directory; verify outputs, cleanup, no plaintext passwords on disk and no regex/serialization use
