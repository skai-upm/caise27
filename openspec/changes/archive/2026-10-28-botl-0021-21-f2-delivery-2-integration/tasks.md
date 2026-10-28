# Tasks

## 1. Object relationships

- [x] 1.1 Change `Group` to hold `User` references and adapt `GroupService`, `GroupFileStorage` (DNIs only in the file), `ParkController` and tests; verify all tests pass and a test that membership follows an email change

## 2. Integration

- [x] 2.1 Replace the HELP text with the exact Delivery 2 syntax list; verify a test checking every listed string
- [x] 2.2 Add `DeliveryTwoIntegrationTest` covering both deliveries' commands, restart and `--fly`; verify it passes with the Delivery 1 integration test
- [x] 2.3 Audit restrictions (production imports `java.*`/project only, no regex APIs, test-only dependencies, single controller instance, no `Serializable`) by `grep`; verify no findings
- [x] 2.4 Update `README.md` and `uml/model.puml`; run `mvn clean package` and a JAR session from a temporary directory; verify `BUILD SUCCESS` and the documented commands

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C10] Complete delivery 2 integration`; verify roadmap order with `git log --reverse --format=%s`
