# Tasks

## 1. Central management

- [x] 1.1 Create `park.Park` singleton (private constructor, `getInstance()`), holding the users repository, current date and current price, with `start(UserRepository)`; verify unit tests for same instance, no public constructor, initial date = today, initial price = 40.00
- [x] 1.2 Wire `App.run` to start the park with the file-backed users repository and build the user service from it; verify existing tests pass

## 2. Commands

- [x] 2.1 Add `SYSTEM DATE;<yyyy-MM-dd>` (strict parsing) and `SYSTEM PRICE;<Price>` (positive, ≤ 2 decimals, no regex) to `CommandProcessor`, plus HELP lines; verify unit tests for valid, repeated and invalid values leaving state unchanged

## 3. Integration

- [x] 3.1 Run the JAR with `SYSTEM DATE`/`SYSTEM PRICE` commands (valid and invalid) and verify responses; run `mvn clean package` and verify all tests pass
