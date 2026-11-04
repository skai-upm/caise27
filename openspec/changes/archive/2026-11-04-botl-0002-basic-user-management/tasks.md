# Tasks

## 1. User model

- [x] 1.1 Create `User` (encapsulated fields, `final` DNI without setter) and `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest` that all seven values read back and that `UserField` has no DNI constant

## 2. Validation

- [x] 2.1 Implement `UserValidator` (DNI check letter, email, password, card, non-blank) without regular expressions; verify with `UserValidatorTest` covering every valid/invalid example from the specs

## 3. Persistence

- [x] 3.1 Implement `UserFileRepository` load/save of semicolon-separated UTF-8 lines (missing file = empty list, malformed lines skipped); verify with `UserFileRepositoryTest` using a `@TempDir` round-trip and the exact line format

## 4. User operations

- [x] 4.1 Implement `UserException` and `UserManager.create` with validation and DNI/email uniqueness, saving after success; verify with `UserManagerTest` (valid creation, each invalid field, duplicate DNI, duplicate email, file unchanged on rejection)
- [x] 4.2 Implement `UserManager.delete` and `UserManager.update` with email/password authentication, revalidation, email uniqueness and rollback on save failure; verify with `UserManagerTest` (every editable field, credential change sequence from the specs, unauthorized and invalid updates, DNI immutability, restart recovery via a new `UserManager` on the same file)

## 5. Commands and terminal

- [x] 5.1 Implement `CommandProcessor` (split on `;`, operation dispatch, exact parameter counts, `OK`/`ERROR` results, never throws); verify with `CommandProcessorTest` (appendix examples, unknown operation, missing/extra parameters, `DNI` field, blank line)
- [x] 5.2 Replace the Hello World `App` with a stdin command loop wired to `users.txt`, remove the placeholder `AppTest`, and add `users.txt` to `.gitignore`; verify by piping the appendix commands into `java -jar` twice and observing that the second run sees the stored users

## 6. Integration checks

- [x] 6.1 Run `mvn clean test` and confirm all tests pass with zero failures
