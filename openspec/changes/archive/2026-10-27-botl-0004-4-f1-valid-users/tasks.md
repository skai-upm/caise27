# Tasks

## 1. Validation rules

- [x] 1.1 Create `model/UserValidator` with `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard` and `isValid(User)` using only char/String operations; verify with `UserValidatorTest` covering every valid and invalid example from the source plus null input
- [x] 1.2 Verify no regex usage in the validator (`grep -E "regex|matches\(|replaceAll|split\(" UserValidator.java` finds nothing) and that `mvn package` passes

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F1-C02] Add user data validation`; verify with `git log -1 --format=%s`
