# Tasks

## 1. Project setup

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `parque`), move it to the repository root and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `pom.xml` for Java release 21, `finalName` `parque` and a `maven-jar-plugin` manifest with main class `es.upm.parque.App`; verify `mvn test` passes and `mvn package` produces `target/parque.jar` runnable with `java -jar`
- [x] 1.3 Ignore runtime data (`data/`) in `.gitignore` and verify `git status` does not show generated data files

## 2. User model and validation

- [x] 2.1 Implement `User` with encapsulated attributes and a final DNI (no setter); verify with a unit test that getters return constructor values and setters change only editable fields
- [x] 2.2 Implement the `UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) with case-insensitive lookup that rejects `DNI`/unknown values; verify with a unit test
- [x] 2.3 Implement `UserValidator` (DNI check letter, email, password, card, non-empty) without regular expressions; verify unit tests cover every valid/invalid example of the user-management spec

## 3. Persistence

- [x] 3.1 Implement `UserRepository` with configurable path: `save` writes one semicolon-separated line per user, `load` returns users, returns empty when the file is missing and skips malformed lines with a warning; verify with unit tests using a temporary directory (round trip, missing file, malformed line)

## 4. User management rules

- [x] 4.1 Implement `UserManager.create` with validation and DNI/email uniqueness, saving after success; verify unit tests for valid creation, each invalid field, duplicate DNI and duplicate email
- [x] 4.2 Implement `UserManager.delete` (email lookup, password check, save); verify unit tests for success, unknown email and wrong password, and that the file no longer contains the user
- [x] 4.3 Implement `UserManager.update` for every `UserField`, re-validating restricted values and email uniqueness, saving after success; verify unit tests for each field, wrong credentials, invalid new values and email taken by another user
- [x] 4.4 Verify with a unit test that users created through one `UserManager` are loaded by a new `UserManager` on the same file (restart scenario)

## 5. Command processing and execution modes

- [x] 5.1 Implement `CommandProcessor` parsing with `String.split(";", -1)`, dispatching `USER CREATE/DELETE/UPDATE`, printing `OK`/`ERROR` results and handling unknown commands and wrong parameter counts; verify with unit tests capturing output
- [x] 5.2 Implement `HELP` (syntax of all commands) and `EXIT` (returns stop signal); verify with unit tests
- [x] 5.3 Implement `App` mode selection (no args → standard input, one arg → file) using one shared run loop that skips blank lines and stops at `EXIT`; verify with unit tests that file mode executes in order, ignores blank lines and does not execute commands after `EXIT`
- [x] 5.4 Handle nonexistent/unreadable input files with an error message and controlled termination; verify with a unit test that no exception escapes `App`

## 6. Integration

- [x] 6.1 Run `mvn test` and verify all tests pass; build `target/parque.jar` with `mvn package` and verify a command file (`USER CREATE`, `USER UPDATE`, `HELP`, `EXIT`, trailing command) runs end to end via `java -jar`, then run it again to verify users are restored
- [x] 6.2 Resolve the SonarQube quality-gate violation reported on new code (rule java:S3516 flagged on the password validation method) without changing validation behavior; verify `mvn test` still passes and the next SDDX measurement reports zero new violations
