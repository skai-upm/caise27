# Tasks

## 1. User model

- [x] 1.1 Create `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`) and `User` with private fields, final DNI and setters only for editable fields; verify with `UserTest` that all seven values are retained and there is no DNI setter

## 2. Validation

- [x] 2.1 Implement `UserValidator` (DNI check letter, email, password, card, non-blank) without regex; verify `UserValidatorTest` covers every valid/invalid example in the source

## 3. Persistence

- [x] 3.1 Implement `UserRepository` load/save of `;`-separated lines (missing file → empty, malformed lines skipped); verify `UserRepositoryTest` round-trips users and handles a missing file

## 4. Authenticated operations

- [x] 4.1 Implement `UserService` create with full validation and DNI/email uniqueness, saving after success; verify `UserServiceTest` valid creation and duplicate DNI/email rejection with unchanged file
- [x] 4.2 Implement delete and update with email+password authentication, per-field revalidation, email uniqueness and rollback on failure; verify `UserServiceTest` covers the source update chain, wrong credentials, invalid new values and all six editable fields

## 5. Command parsing and terminal

- [x] 5.1 Implement `CommandProcessor` (split on `;`, exact parameter counts, `OK:`/`ERROR:` responses, no exception escapes); verify `CommandProcessorTest` for unknown operations, missing/extra parameters and the `DNI` field rejection
- [x] 5.2 Rewrite `App` as a stdin command loop wired to `users.txt`; update `AppTest` to run commands through `App` and verify responses and that main returns when input ends

## 6. Integration verification

- [x] 6.1 Verify restart persistence end-to-end (create, update, delete, new `UserService` on same file recovers exact state) and that `mvn test` and `mvn package` succeed
