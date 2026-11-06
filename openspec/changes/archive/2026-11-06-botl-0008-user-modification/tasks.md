# Tasks

## 1. Model and repository

- [x] 1.1 Add enum `es.upm.park.model.UserField` (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) and verify with a unit test that it has exactly these six values and no DNI
- [x] 1.2 Add `UserRepository.replace(old, updated)` (save before swapping) and verify with unit tests that the replacement is reloaded from file and a failed save keeps the original

## 2. Service

- [x] 2.1 Add `UserService.update(email, password, field, value)` with shared authentication, per-field validation and uniqueness, and verify with unit tests: each field updated; DNI/unknown field rejected; wrong password/unknown email rejected; invalid email/password/card and email used by another user rejected with users unchanged; own email accepted

## 3. Command

- [x] 3.1 Dispatch `USER UPDATE` (4 fields) in `CommandProcessor` and verify with unit tests for success, wrong field count and an error case
- [x] 3.2 Add a `MainTest` that updates NAME in one run and verifies it after a restart from the same file

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass and the JAR is produced
- [x] 4.2 Run `openspec validate botl-0008-user-modification --strict` and verify the change is valid
