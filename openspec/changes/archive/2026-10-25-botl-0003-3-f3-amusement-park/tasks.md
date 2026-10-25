# Tasks

## 1. [F3-C01] Reorganize application into layered packages

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller` (managers renamed to controllers), `business.validation`, `persistence`, keep `App` as composition root, move tests to matching packages; verify `mvn test` passes with no behavior change and no class in the default package

## 2. [F3-C03] Add singleton controllers and dependency injection

- [x] 2.1 Turn the controllers into Singletons with `configure(...)` dependency injection, add `StorageLocation`, and wire repositories and collaborators in `App.initialize`; verify tests for same-instance access and for normal vs temporary data directories with unchanged controller code

## 3. [F3-C04] Add extended help generation

- [x] 3.1 Introduce `CommandDefinition` registry in `CommandProcessor`, `HelpFormatter` implementations and `HelpGenerator` injected from `App`; add `HELP EXTENDED`; verify tests that HELP lists every command syntax and HELP EXTENDED adds descriptions

## 4. [F3-C05] Secure stored user passwords

- [x] 4.1 Add `org.mindrot:jbcrypt:0.4`, `PasswordHasher`, hash on create/password change, verify on delete/update, migrate plaintext on load; verify tests that the file holds hashes, authentication works after restart and legacy plaintext is migrated

## 5. [F3-C02] Add invoice generation with builder

- [x] 5.1 Implement `Invoice`, `InvoiceSection`, `InvoiceLine`, `InvoiceBuilder`, `StandardInvoiceBuilder`, `InvoiceController` and `INVOICE CREATE;USER|GROUP;...`; verify tests for section selection, no double counting of internal tickets, group totals and unknown recipients

## 6. [F3-C06] Add speed and special passes

- [x] 6.1 Implement `Pass`, `SpeedPass`, `SpecialPass`, ticket composition, `SPEEDPASS CREATE/DELETE` and `SPECIALPASS CREATE/DELETE` with prices and refund rules; verify tests for quantities 1/10, 20 % price, single Special Pass, refunds allowed/rejected

## 7. [F3-C07] Add generic attraction access types

- [x] 7.1 Implement `Access` interfaces, generic `Attraction<T extends Access>` with the four subclasses and `AttractionType`, optional type in `ATTRACTION CREATE`; verify tests for type admission (GROUP rejects individual, INDIVIDUAL rejects group, default GENERAL)

## 8. [F3-C08] Extend attraction access and pass consumption

- [x] 8.1 Implement `ATTRACTION ENTER ...;SPEED` and SPECIAL automatic Special Pass consumption with atomicity and reporting; verify tests for individual/group SPEED, atomic failure without consumption, SPECIAL consumption and SPEED rejection

## 9. [F3-C09] Extend delivery 3 persistence and UML

- [x] 9.1 Implement `PassRepository`, attraction type column, cascade deletions without partial effects; verify restart tests for passes (price, consumed, ticket), attraction type, cascades and `--fly` with passes
- [x] 9.2 Rewrite `uml/model.puml` for the E3 design; verify the PlantUML `-syntax` check and compare against the source classes

## 10. [F3-C10] Complete delivery 3 integration

- [x] 10.1 Configure `maven-shade-plugin` so `mvn package` builds an executable `target/parque.jar` with BCrypt; update README; verify `java -jar` runs E1–E3 command files normally and with `--fly`, and HELP/HELP EXTENDED list all commands
