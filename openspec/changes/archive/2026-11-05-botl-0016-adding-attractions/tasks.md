# Tasks

## 1. Model

- [x] 1.1 Create `model.Attraction` (identifier, name, capacity, occupants with all-or-nothing admission never exceeding capacity) and `repository.AttractionRepository` exposed by `Park`; verify unit tests for capacity invariant and that `User` has no attraction reference

## 2. Commands

- [x] 2.1 Create `service.AttractionService` and register `ATTRACTION CREATE`/`ATTRACTION LIST` with HELP lines; verify command tests for valid creation, invalid data, empty/non-empty listing (state unchanged) and unknown attraction

## 3. Integration

- [x] 3.1 Run the JAR with the assignment's attraction examples and `ATTRACTION LIST`; run `mvn clean package` and verify all tests pass
