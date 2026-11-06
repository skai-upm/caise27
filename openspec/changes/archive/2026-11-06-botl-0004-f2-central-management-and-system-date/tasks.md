# Tasks

## 1. Central park management

- [x] 1.1 Add `es.upm.park.controller.ParkController` with a private constructor, `getInstance()`, the current date (startup local date) and base price (40.00), and validated setters, and verify with `ParkControllerTest` that two `getInstance()` calls return the same object, the initial values are correct, and invalid values are rejected without changing state

## 2. SYSTEM commands

- [x] 2.1 Refactor `CommandProcessor` to dispatch on the `;`-split command keyword, keeping HELP/EXIT/unknown behavior, and verify that the existing `CommandProcessorTest` cases still pass
- [x] 2.2 Implement `SYSTEM DATE;<yyyy-MM-dd>` (valid date updates the controller, invalid or missing values give an error and no change), and verify with tests for a valid date, repeated changes, `2027-02-30`, `15/04/2027` and a missing value
- [x] 2.3 Implement `SYSTEM PRICE;<Price>` (positive, at most 2 decimals; otherwise an error and no change), and verify with tests for `45.50`, `0`, `-5`, `abc`, `10.999` and a missing value
- [x] 2.4 Add the SYSTEM commands to the HELP text and README, and verify with a test that HELP contains both syntaxes

## 3. Integration

- [x] 3.1 Run `mvn clean package` and a file-mode run of the JAR with `SYSTEM DATE;2027-04-15`, `SYSTEM PRICE;45.50`, an invalid price and `EXIT`, and verify the confirmations and the error message
