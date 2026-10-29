# Tasks

## 1. Packages

- [x] 1.1 Move classes with `git mv` into `presentation`, `business.model`, `business.control`, `persistence` (App stays in the root), update package/import declarations and visibility of cross-package members; verify `mvn -q compile` succeeds and `Main-Class` is still `es.upm.amusementpark.App`
- [x] 1.2 Move `--fly` directory creation/deletion from `App` into `persistence.DataDirectories`; verify existing `AppTest` fly tests pass

## 2. Exceptions

- [x] 2.1 Add `business.exception` hierarchy and replace every `IllegalArgumentException` thrown by business code with the matching subclass (messages unchanged); add `PersistenceException` for repository I/O and `InvalidCommandException` for argument-count errors; handle them in `CommandProcessor`/`App`; verify with tests asserting the specific exception types for not-found, duplicate, invalid value and rule-violation cases, and that processing continues after each

## 3. Verification

- [x] 3.1 Add `ArchitectureTest` enforcing the layer dependency rules and no file I/O in business sources; verify it passes
- [x] 3.2 Update tests to the new packages and exception types, update `uml/model.puml` package names; run `mvn clean package` and verify all tests pass and the JAR runs a Delivery 2 command file with unchanged output
