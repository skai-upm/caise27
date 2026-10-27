# Tasks

## 1. Package reorganisation

- [x] 1.1 Move classes into `presentation`, `business.model`, `business.controller`, `persistence`, `persistence.repository` (git mv), update `pom.xml` main class and tests; verify `mvn package` and `java -jar target/nombre-aplicacion.jar` still run

## 2. Exceptions and business controllers

- [x] 2.1 Add the `business.exception` hierarchy and the User/Group/Ticket/Attraction controllers throwing it; verify with `BusinessControllersTest` (each exception type raised for its situation)
- [x] 2.2 Rewrite presentation handlers (`CommandInput`, `InvalidCommandException`, central exception mapping) and move `--fly` directory handling into `ParkController`; verify all Delivery 1 and 2 regression tests pass unchanged
- [x] 2.3 Add layering checks to `TechnicalRestrictionsTest` (no default package, presentation imports no persistence except `PersistenceException`, model imports no persistence) and adjust `uml/model.puml` package names; verify they pass

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C01] Reorganize application into layered packages`; verify with `git log -1 --format=%s`
