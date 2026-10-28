# Tasks

## 1. Park controller

- [x] 1.1 Create the `ParkController` singleton with current date (`LocalDate.now()` at start) and base price `40.00`; verify with unit tests for same-instance access and initial state
- [x] 1.2 Add validated setters for date and price (format/positivity checks without regex); verify with unit tests for valid and rejected values

## 2. SYSTEM commands

- [x] 2.1 Implement `SYSTEM DATE;<yyyy-MM-dd>` and register it; verify with command tests for valid, repeated and invalid dates
- [x] 2.2 Implement `SYSTEM PRICE;<Price>` and register it; verify with command tests for valid and invalid prices (0, negative, text, 3 decimals, missing)
- [x] 2.3 Add both commands to the HELP listing; verify with the HELP unit test

## 3. Delivery

- [x] 3.1 Run `mvn clean package` and execute the JAR with a command file containing `SYSTEM DATE;2027-04-15`, `SYSTEM PRICE;45.50`, `EXIT`; verify success messages and BUILD SUCCESS
- [x] 3.2 Commit with the exact message `[F2-C01] Add park controller and system date`; verify with `git log -1 --format=%s`
