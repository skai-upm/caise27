# Tasks

## 1. User model

- [x] 1.1 Create `User` with encapsulated fields, final DNI and setters only for editable fields, plus the `UserField` enum; verify with `UserTest` (getters return constructor values, no DNI setter, `UserField` has no DNI and parses names case-insensitively)

## 2. Validation

- [x] 2.1 Implement `UserValidator` (DNI letter algorithm, email via split/indices, password categories, 16-digit card, non-blank); verify `UserValidatorTest` covers every valid and invalid example of the source

## 3. Persistence

- [x] 3.1 Implement `UserFileStorage` with injectable path, `load` (missing file → empty, blank/malformed lines skipped) and `save` (one `;`-separated line per user); verify `UserFileStorageTest` round-trips users through a temporary file and checks the exact line format

## 4. Authenticated operations

- [x] 4.1 Implement `UserService` create (validation then DNI/email uniqueness) with save after success; verify tests for valid creation, each invalid field and duplicate DNI/email, with the file unchanged on rejection
- [x] 4.2 Implement delete and update (authentication, revalidation, email uniqueness on update, DNI immutability, revert on save failure); verify tests for every editable field, the chained EMAIL→PASSWORD→CARD example, invalid values, wrong password and unknown email

## 5. Command parsing and terminal

- [x] 5.1 Implement `CommandProcessor` using `String.split` with parameter-count checks and `OK:`/`ERROR:` responses; verify `CommandProcessorTest` covers all three commands, unknown operations, missing parameters, the `DNI` field and that no exception escapes
- [x] 5.2 Turn `App` into the terminal loop wiring storage, service and processor (no file logic in `App`); verify `AppTest` feeds commands through `System.in` and checks output and resulting file

## 6. Integration verification

- [x] 6.1 Verify restart persistence: a test runs create/update/delete, builds a new service over the same file and checks only the expected users/values are recovered; then `mvn clean test` passes and `java -jar` processes piped commands
