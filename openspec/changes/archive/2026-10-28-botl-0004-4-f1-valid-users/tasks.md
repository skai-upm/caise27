# Tasks

## 1. Validation rules

- [x] 1.1 Implement `UserValidator.isValidDni` (8 digits + matching uppercase control letter); verify unit tests for `12345678Z` valid, `12345678A` invalid and malformed lengths/characters invalid
- [x] 1.2 Implement `UserValidator.isValidEmail` with String operations and `split`, no regex; verify unit tests for every valid/invalid example of the statement
- [x] 1.3 Implement `UserValidator.isValidPassword` without regex; verify unit tests for every valid/invalid example of the statement
- [x] 1.4 Implement `UserValidator.isValidCardNumber`; verify unit tests for every valid/invalid example of the statement
- [x] 1.5 Implement `UserValidator.isValid(User)` combining the four rules and ignoring the telephone; verify unit tests with an all-valid user and users with one invalid field each
- [x] 1.6 Run `mvn package`; verify `BUILD SUCCESS` with all tests passing

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C02] Add user data validation`; verify with `git log -1 --format=%s`
