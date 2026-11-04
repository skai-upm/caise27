# Tasks

## 1. User model

- [x] 1.1 Create `User` (encapsulated seven fields, no DNI setter, copy constructor) and `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest` that getters return constructor values, editable setters work and no DNI mutator exists

## 2. Validation

- [x] 2.1 Implement `UserValidator` DNI, email, password, card and non-blank checks without regular expressions; verify with `UserValidatorTest` covering every valid/invalid example in the source (DNI `12345678Z`/`12345678A`, the five invalid emails, four invalid passwords, three invalid cards) plus shape edge cases

## 3. File persistence

- [x] 3.1 Implement `UserFileStore` load/save of semicolon-separated lines (missing file = empty, malformed lines skipped, temp-file + move on save); verify with `UserFileStoreTest` (round trip, exact line format, missing file, malformed line)

## 4. Authenticated user operations

- [x] 4.1 Implement `UserManager.create` with full validation and DNI/email uniqueness, persisting on success; verify with `UserManagerTest` (valid create, each invalid field, duplicate DNI, duplicate email, file unchanged on rejection)
- [x] 4.2 Implement `UserManager.delete` and `UserManager.update` with credential checks, per-field revalidation and email uniqueness; verify with `UserManagerTest` (all six fields, credential change takes effect, wrong password, unknown email, invalid values, duplicate email, DNI unchanged)
- [x] 4.3 Verify restart recovery: a second `UserManager` on the same file after create/update/delete sees exactly the expected users (`UserManagerTest`)

## 5. Command processing and terminal

- [x] 5.1 Implement `CommandProcessor` (split with `String.split`, parameter-count checks, `UserField` parsing, `OK:`/`ERROR:` messages, no exceptions escape); verify with `CommandProcessorTest` using the source's example commands and malformed/unknown commands
- [x] 5.2 Rewrite `App` as a stdin loop delegating to `CommandProcessor` with `users.txt`, no file I/O in `App`; verify `mvn test` passes and `java -jar` with piped example commands prints one result per command and exits 0
