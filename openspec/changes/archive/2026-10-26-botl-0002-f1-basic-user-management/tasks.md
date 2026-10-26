# Tasks

## 1. User model and validation

- [x] 1.1 Create `User` (encapsulated fields, immutable `dni`, setters only for editable fields) and `UserField` enum with `parse`; verify with `UserTest`/`UserFieldTest` (getters, setters, parse of valid/unknown/`DNI` names)
- [x] 1.2 Implement `UserValidator` (DNI check letter, email `texto@texto.xxx`, password classes, 16-digit card, not-blank) without regex; verify `UserValidatorTest` covers every valid/invalid example from the spec

## 2. Persistence

- [x] 2.1 Implement `UserRepository` with `loadAll`/`saveAll` over a `;`-separated file (one user per line, missing file = empty list, parent dirs created, malformed lines skipped); verify `UserRepositoryTest` round-trips users in a `@TempDir` and checks the exact line format
- [x] 2.2 Add `data/` to `.gitignore`; verify `git check-ignore data/users.txt` reports it ignored

## 3. User management rules

- [x] 3.1 Implement `UserManager.create` with validation, DNI/email uniqueness and save; verify `UserManagerTest` for success, each invalid field, duplicate DNI and duplicate email (no user stored on failure)
- [x] 3.2 Implement `UserManager.delete` and `UserManager.update` (credential check, field-specific validation, email uniqueness excluding self, DNI rejected, save, reload on save failure); verify `UserManagerTest` for success and every failure scenario in the spec, including persistence after reload

## 4. Command interface

- [x] 4.1 Implement `CommandProcessor` (split with `;`, trim, case-insensitive operation, parameter-count checks, `OK`/`ERROR` responses, catch-all for unexpected errors); verify `CommandProcessorTest` for each command, unknown command, wrong parameter count and unexpected exception
- [x] 4.2 Rewrite `App` to wire repository/manager/processor with `data/users.txt` and loop over standard input via `run(BufferedReader, PrintStream)`; verify `AppTest` drives `run` with two commands and a blank line, and that a restart over the same file sees the created user
- [x] 4.3 Run `mvn package` and an end-to-end check piping the spec examples into `java -jar target/parque.jar` from a temporary working directory; verify outputs and the resulting `data/users.txt`
