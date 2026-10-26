# Tasks

## 1. User model and validation

- [x] 1.1 Create `User` (encapsulated fields, immutable DNI) and `UserField` enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD); verify with `mvn compile`
- [x] 1.2 Implement `UserValidator` (DNI check letter, email `texto@texto.xxx`, password classes, 16-digit card, non-empty) without regex; verify with `UserValidatorTest` covering every valid/invalid example of the specs

## 2. Persistence

- [x] 2.1 Implement `UserStorage` load/save of the `;`-separated file (missing file → empty, malformed lines skipped); verify with `UserStorageTest` round-trip using a temp directory
- [x] 2.2 Add `data/` to `.gitignore`; verify `git check-ignore data/users.txt` succeeds

## 3. User operations

- [x] 3.1 Implement `UserManager` create/delete/update with validation, DNI/email uniqueness, credential checks, non-editable DNI and save after each change; verify with `UserManagerTest` (duplicates, wrong password, update re-validation, persistence after change and reload)

## 4. Command processing and terminal

- [x] 4.1 Implement `CommandProcessor` splitting with `String.split`, parameter-count checks, unknown-command handling and catch-all error responses; verify with `CommandProcessorTest` (valid commands, unknown command, insufficient parameters, blank line)
- [x] 4.2 Turn `App` into the terminal loop using `data/users.txt`; verify by piping commands into `java -jar target/parque.jar`, restarting, and checking the user is restored and duplicates rejected
- [x] 4.3 Run `mvn test` and verify the whole suite passes
