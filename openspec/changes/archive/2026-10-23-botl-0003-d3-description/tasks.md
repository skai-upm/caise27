# Tasks

## 1. Layering, exceptions and build

- [x] 1.1 Move classes into `presentation`, `business.{model,controller,validation,exception,security,invoice}`, `persistence[.file]` packages and replace `OperationException` with the `ParkException` hierarchy; verify `mvn -q compile` and that no presentation class imports `persistence`
- [x] 1.2 Add jBCrypt dependency and assembly packaging (self-contained `target/amusement-park.jar`); verify `mvn package` and `java -jar target/amusement-park.jar` runs `HELP`

## 2. Dependency injection and singletons

- [x] 2.1 Introduce repository interfaces, `Repositories`, `FileRepositories`, `PersistenceException`; make `Park` and every controller a configured singleton wired in `App.initialize`; verify `SingletonWiringTest` (single instances, unconfigured use fails, repositories swapped by re-initialising with other paths) and the migrated Delivery 1–2 test suites pass

## 3. Passwords

- [x] 3.1 Add `PasswordHasher`/`BCryptPasswordHasher`, store hashes, verify on delete/update, re-hash on password change; verify `PasswordProtectionTest` (no plain password in file, `$2a$` prefix, wrong password, legacy plain line never authenticates, changed password works after restart)

## 4. Passes

- [x] 4.1 Add `Pass`/`SpeedPass`/`SpecialPass`, `PassController` (create 1|10 with derived ids and up-front conflict check, special at most one, refunds) and commands; verify `PassControllerTest` (prices incl. discounted component, totals, ids, conflicts, refund rules)
- [x] 4.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE`; verify `PassCascadeTest`

## 5. Attraction types and access

- [x] 5.1 Add access interfaces, generic `Attraction<T>` hierarchy and `AttractionType`; typed `ATTRACTION CREATE` (4 or 5 fields); verify `AttractionAccessTest` type cases and `CommandProcessorTest` (accepted ticket kinds per type, default GENERAL, unknown type)
- [x] 5.2 Extend `ATTRACTION ENTER` with `;SPEED` and automatic Special Pass use, atomic for groups, output naming consumed passes; verify `AttractionAccessTest` (individual/group SPEED, missing pass → nothing changes, SPECIAL rules)

## 6. Invoices

- [x] 6.1 Add invoice Builder classes, `InvoiceController` (director), `InvoiceFormatter` and `INVOICE CREATE` commands; verify `InvoiceTest` (Ana 167.20 example, G01 108.00 with composition, sections omitted when empty, unknown targets)

## 7. Help

- [x] 7.1 Add `HelpGenerator` with injected `HelpFormatter` (syntax / extended) and `HELP EXTENDED`; verify `HelpTest` (every syntax from the specification in HELP, every command with description in HELP EXTENDED)

## 8. Persistence

- [x] 8.1 Persist attraction type (default GENERAL) and passes (`passes.txt`); verify `ParkPersistenceTest` additions (types, pass price/consumed state, Delivery 2 attraction line) and `--fly` cleanup with passes (`AppTest`, JAR run)

## 9. Documentation and integration

- [x] 9.1 Update `uml/model.puml` (access interfaces, generics, attraction types, passes with cardinalities, builders, DI relationships, singleton controllers) and README; verify PlantUML syntax and class/field cross-check against sources
- [x] 9.2 Run `mvn package`, then the specification's Delivery 3 example with the JAR normally and with `--fly`; verify outputs (SP01 total 80.00, PE01 40.00, consumed passes reported, invoices) and that Delivery 1–2 example files still run without errors
