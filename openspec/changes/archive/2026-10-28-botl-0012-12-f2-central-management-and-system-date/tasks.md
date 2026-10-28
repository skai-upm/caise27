# Tasks

## 1. Park controller

- [x] 1.1 Create `park.ParkController` singleton with `initialize(UserStorage)`, `users()`, current date (startup = `LocalDate.now()`) and base price (startup = 40.00); verify unit tests: same instance, no public constructor, initial date/price, re-initialization resets price
- [x] 1.2 Wire `App` and `CommandProcessor` through the controller (users loaded via `initialize`); verify all existing tests pass unchanged in behavior

## 2. System commands

- [x] 2.1 Add `SYSTEM DATE;<yyyy-MM-dd>` (strict date parsing, repeatable) and `SYSTEM PRICE;<Price>` (positive, ≤2 decimals) to `CommandProcessor`; verify unit tests for valid changes, repeated changes and each invalid example
- [x] 2.2 Extend `HELP` with `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`; verify the help unit test
- [x] 2.3 Run `mvn package` and the JAR with a file using both commands; verify `BUILD SUCCESS` and the responses

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C01] Add park controller and system date`; verify with `git log -1 --format=%s`
