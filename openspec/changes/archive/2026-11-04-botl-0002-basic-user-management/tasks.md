# Tasks

## 1. User model

- [x] 1.1 Create `UserField` enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) and `User` with private fields, getters, setters for editable fields and a final DNI without setter; verify with `UserTest` (fields retained, no DNI setter)

## 2. Validation

- [x] 2.1 Implement `UserValidator` (DNI check letter, email, password categories, 16-digit card, nonblank) without regex; verify with `UserValidatorTest` covering every valid/invalid example from the source

## 3. Persistence

- [x] 3.1 Implement `UserFileStorage` load/save of semicolon lines (missing file → empty list, malformed lines skipped); verify with `UserFileStorageTest` round trip using a temporary directory and exact line format

## 4. User operations

- [x] 4.1 Implement `UserManager` create with full validation and DNI/email uniqueness, saving after success; verify with `UserManagerTest` (valid create, each invalid field, duplicate DNI, duplicate email, file unchanged on rejection)
- [x] 4.2 Implement authenticated delete and update (all six fields, revalidation, email uniqueness excluding self, unchanged data on failure); verify with `UserManagerTest` cases for wrong password, unknown email, each field update and the email→password→card sequence
- [x] 4.3 Verify restart recovery: a new `UserManager` on the same file sees only the current users and values (test in `UserManagerTest`)

## 5. Command processing and terminal

- [x] 5.1 Implement `CommandProcessor` using `String.split(";", -1)` with OK/ERROR messages, unknown-operation, wrong-count, unsupported-field and exception safety; verify with `CommandProcessorTest`
- [x] 5.2 Rewrite `App` to load users from `users.txt` (or first argument), read stdin line by line and print results; verify with `AppTest` feeding commands through a redirected `System.in` and with `mvn package` plus a manual `java -jar` run

## 6. Verification

- [x] 6.1 Run `mvn clean test` and confirm all tests pass, and review that file I/O exists only in `UserFileStorage`
