# Tasks

## 1. Central management

- [x] 1.1 Create `ParkController` singleton holding current date (start: local date) and base price (start: 40.00) with `reset()`; verify unit tests for same instance, private constructor and initial values

## 2. SYSTEM commands

- [x] 2.1 Implement `SYSTEM DATE;<yyyy-MM-dd>` with validation; verify unit tests for valid, repeated, invalid and missing dates leaving state unchanged on failure
- [x] 2.2 Implement `SYSTEM PRICE;<Price>` with validation; verify unit tests for valid prices and rejection of 0, negative, non-numeric, >2 decimals and missing values
- [x] 2.3 Wire SYSTEM commands in `App.run` (resetting state at start) and list them in `HELP`; verify via an `App.run` test

## 3. Verification

- [x] 3.1 Run `mvn -q package` and verify all tests pass and the JAR processes a file with `SYSTEM DATE;2027-04-15`, `SYSTEM PRICE;45.50`, `EXIT`
