# Tasks

## 1. User model and validation

- [x] 1.1 Create `User` (encapsulated fields, immutable `dni`, setters only for editable fields) and `UserField` enum with `parse`; verify with `UserTest`/`UserFieldTest` (parse is case-insensitive, `DNI` and unknown names return null)
- [x] 1.2 Create `UserValidator` (DNI check letter, email, password, card, non-blank) without regular expressions; verify with `UserValidatorTest` covering every valid/invalid example of the spec

## 2. Persistence

- [x] 2.1 Create `UserRepository` with `load()` and `save(...)` over a configurable path (semicolon-separated, one user per line, missing file = empty, malformed lines ignored, parent directories created); verify with `UserRepositoryTest` using a JUnit `@TempDir`
- [x] 2.2 Add `data/` to `.gitignore` and verify `git check-ignore data/users.txt` matches

## 3. User operations

- [x] 3.1 Create `UserManager` with create (validation order and duplicates), delete (credentials) and update (credentials, field rules, duplicate email, invalid field), saving after each success; verify with `UserManagerTest` covering all spec scenarios, including restart through a new manager on the same file

## 4. Command processing and application

- [x] 4.1 Create `CommandProcessor` (split by `;`, trimmed parameters, case-insensitive operation, parameter counts, `ERROR UNKNOWN COMMAND`, `ERROR WRONG NUMBER OF PARAMETERS`, exception safety); verify with `CommandProcessorTest`
- [x] 4.2 Rewrite `App` to wire the components and loop over standard input (`run(BufferedReader, PrintStream)`, blank lines ignored); replace the placeholder `AppTest` with tests of `run`; verify `mvn test` passes
- [x] 4.3 Verify end to end: `mvn package` and pipe the spec's CREATE/UPDATE/DELETE examples into `java -jar target/parque.jar` in a scratch directory, checking responses and the resulting `data/users.txt`
