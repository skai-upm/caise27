# Tasks

## 1. User model and validation

- [x] 1.1 Create `User` (encapsulated fields, immutable DNI) and the `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`, lookup by name); verify with unit tests that DNI has no setter path and `UserField.fromName("DNI")` is null
- [x] 1.2 Implement `UserValidator` (DNI control letter, email, password, 16-digit card) without regex; verify with unit tests covering every valid/invalid example of the epic

## 2. Persistence

- [x] 2.1 Implement `UserRepository` to load and save users as `;`-separated lines (missing file → empty list, malformed lines skipped, parent directory created); verify with a `@TempDir` round-trip test

## 3. User management rules

- [x] 3.1 Implement `UserManager.create` with validation and DNI/email uniqueness, saving after success; verify with unit tests for valid creation, each invalid field, duplicate DNI and duplicate email
- [x] 3.2 Implement `UserManager.delete` (email lookup + password check, save); verify with tests for success, unknown email and wrong password
- [x] 3.3 Implement `UserManager.update` (credential check, field enum, revalidation, email uniqueness, save); verify with tests for each field, wrong credentials, DNI field rejected, duplicate email, weak password, bad card

## 4. Command processing and application

- [x] 4.1 Implement `CommandProcessor` (split with `;`, dispatch `USER CREATE/DELETE/UPDATE`, parameter-count checks, unknown command, `ERROR` responses); verify with unit tests including insufficient parameters and unknown command
- [x] 4.2 Update `App` to load users from `data/users.txt` and process stdin line by line, ignoring blank lines; add `data/` to `.gitignore`; verify by piping commands to `java -jar target/parque.jar` and restarting to see persisted users rejected as duplicates
- [x] 4.3 Run `mvn test` and verify the build succeeds with all tests passing
