# Tasks

## 1. Central management

- [x] 1.1 Create `park.ParkController` singleton (holder idiom) with `startExecution(UserRepository)`, `getUsers()`, `getCurrentDate()/setCurrentDate`, `getBasePrice()/setBasePrice`; verify `ParkControllerTest`: same instance twice, initial date = `LocalDate.now()`, initial price 40.00, reset by `startExecution`
- [x] 1.2 Make `CommandProcessor` obtain users through `ParkController` (constructor `CommandProcessor(UserRepository)` starts a new execution; add no-arg constructor using the current instance) and wire `App` to start the execution; verify existing tests still pass and a created user is visible via `ParkController.getInstance().getUsers()`

## 2. System commands

- [x] 2.1 Implement `SYSTEM DATE;<yyyy-MM-dd>` with strict parsing; verify tests: valid date, repeated change, invalid inputs (`2027-02-30`, `15/04/2027`, `2027-4-15`, missing) leave date unchanged
- [x] 2.2 Implement `SYSTEM PRICE;<Price>` with non-regex validation (positive, ≤2 decimals); verify tests: `45.50` accepted, `0`, `-5`, `abc`, `45.555`, `1E2`, missing rejected with price unchanged; price back to 40.00 after `startExecution`
- [x] 2.3 Add both commands to `HELP` and verify the help test checks them

## 3. Integration

- [x] 3.1 End-to-end with the JAR: `SYSTEM DATE;2027-04-15`, `SYSTEM PRICE;45.50`, invalid variants, `HELP`; verify responses and exit status 0; run full `mvn package` for regression
