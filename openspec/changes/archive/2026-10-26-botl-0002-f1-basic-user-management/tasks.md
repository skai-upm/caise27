# Tasks

## 1. User model and validation

- [x] 1.1 Create `User` with encapsulated attributes and an immutable DNI, and the `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with a unit test that getters/setters work and there is no DNI setter
- [x] 1.2 Implement `UserValidator` (DNI check letter, email `texto@texto.xxx`, password categories, 16-digit card) without regular expressions; verify `UserValidatorTest` covers every valid/invalid example from the spec

## 2. Persistence

- [x] 2.1 Implement `UserRepository` that saves users as `;`-separated lines and loads them back (skipping malformed lines); verify a round-trip test in a temporary directory and the exact line format
- [x] 2.2 Add `data/` to `.gitignore`; verify `git check-ignore data/users.txt` reports it ignored

## 3. User management rules

- [x] 3.1 Implement `UserManager.create` with full validation and DNI/email uniqueness; verify tests for valid creation, each invalid field, duplicate DNI and duplicate email
- [x] 3.2 Implement `UserManager.delete` checking email existence and password; verify tests for success, unknown email and wrong password, and that the file is updated
- [x] 3.3 Implement `UserManager.update` for every `UserField` with credential check and revalidation (email format/uniqueness, password, card); verify tests for each field, wrong credentials and rejected values
- [x] 3.4 Verify users persisted by one `UserManager` are loaded by a new instance over the same file (restart test)

## 4. Command interface

- [x] 4.1 Implement `CommandProcessor` parsing with `String.split`, parameter-count checks, `OK`/`ERROR` responses and unknown-command handling; verify `CommandProcessorTest` covers USER CREATE/DELETE/UPDATE, unknown operation, missing parameters and invalid field name
- [x] 4.2 Make `App.main` read standard input line by line and print each response until input ends; verify by piping the example commands into `java -jar target/parque-1.0-SNAPSHOT.jar`
- [x] 4.3 Run `mvn test` and verify the whole suite passes
