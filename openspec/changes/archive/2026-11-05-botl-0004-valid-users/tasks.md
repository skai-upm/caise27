# Tasks

## 1. Validation rules

- [x] 1.1 Implement `UserValidator.isValidDni` (8 digits + correct check letter); verify unit tests for "12345678Z" accepted, "12345678A" and malformed values rejected
- [x] 1.2 Implement `UserValidator.isValidEmail` with String operations and split, no regex matching; verify unit tests for all valid/invalid examples of the assignment
- [x] 1.3 Implement `UserValidator.isValidPassword` with character scanning; verify unit tests for "Clave1!" accepted and the four invalid examples rejected
- [x] 1.4 Implement `UserValidator.isValidCardNumber`; verify unit tests for 16 digits accepted and spaced/short/long/letter values rejected
- [x] 1.5 Implement `UserValidator.isValid(User)` combining the rules and ignoring the telephone; verify unit tests for a valid user and a user with one invalid field

## 2. Regression

- [x] 2.1 Run `mvn clean package` and verify all tests pass
