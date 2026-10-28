# Tasks

## 1. Validation

- [x] 1.1 Add `UserValidator` with DNI, e-mail, password (delegating to `PasswordPolicy`) and card checks and whole-user `validate`; verify with parameterized unit tests using every valid/invalid example from the source and a no-regex check of the class source

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS)
- [x] 2.2 Commit with the exact message `[F1-C02] Add user data validation`; verify with `git log -1 --format=%s`
