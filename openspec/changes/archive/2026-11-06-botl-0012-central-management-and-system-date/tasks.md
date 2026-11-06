# Tasks

## 1. Central management

- [x] 1.1 Create `es.upm.park.controller.ParkController` (private constructor, `getInstance()`, `start(users, today)`, date/price getters/setters with validation) and verify with unit tests: same instance from several calls, initial date = given today, initial price 40.00, `start` resets price, invalid price/date rejected without change

## 2. Commands

- [x] 2.1 Switch `CommandProcessor` to obtain services through `ParkController`, wire it in `Main` with `LocalDate.now()`, and update existing tests; verify the full test suite still passes
- [x] 2.2 Add `SYSTEM DATE` and `SYSTEM PRICE` (1 field each) with confirmations and errors, and list them in `HELP`; verify with unit tests for valid, repeated, invalid and wrong-field-count cases and with a `MainTest` showing the price is 40.00 again in a new execution

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with `SYSTEM DATE;2027-04-15`, `SYSTEM PRICE;45.50`, `SYSTEM PRICE;-1`, `HELP` and verify the outputs
- [x] 3.2 Run `openspec validate botl-0012-central-management-and-system-date --strict` and verify the change is valid
