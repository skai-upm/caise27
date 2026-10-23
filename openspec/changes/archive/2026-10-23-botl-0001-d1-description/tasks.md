# Tasks

## 1. Project scaffolding and executable JAR

- [x] 1.1 Generate the Maven project from `maven-archetype-quickstart` 1.5 (groupId `es.upm.etsisi`, artifactId `amusement-park`, package `es.upm.etsisi.park`) at the repository root and verify `mvn -q compile` succeeds
- [x] 1.2 Configure `pom.xml` for Java 21 (`maven.compiler.release`), `finalName` `amusement-park` and a `Main-Class` manifest entry; verify `mvn package` produces `target/amusement-park.jar` and `java -jar target/amusement-park.jar` starts

## 2. User model and validation

- [x] 2.1 Add `User` with encapsulated attributes and an immutable DNI, plus the `UserField` enum (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD); verify with a unit test that setters change fields and no DNI setter exists
- [x] 2.2 Add `UserValidator` (DNI checksum, email, password, card, non-empty text) without regular expressions; verify with unit tests covering every valid/invalid example in the user-management spec

## 3. Persistence

- [x] 3.1 Add `UserRepository` that loads users from the `;`-separated text file (missing file → empty, corrupt lines skipped with warning) and saves them atomically; verify with unit tests for round-trip, missing file, and corrupt line handling

## 4. User operations

- [x] 4.1 Add `UserService` with create (validation + DNI/email uniqueness), delete (email + password) and update (email + password, enum field, same validation, email uniqueness) that persists after every successful change and rolls back on save failure; verify with unit tests for each spec scenario including persistence across a new service instance

## 5. Command interface and execution modes

- [x] 5.1 Add `CommandProcessor` that parses `;`-separated lines, dispatches `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`, and reports unknown/malformed commands with `ERROR:` lines; verify with unit tests for dispatch, wrong argument counts, unknown commands, HELP content, and EXIT stopping processing
- [x] 5.2 Implement `App` with interactive mode (stdin) and file mode (first argument, blank lines ignored, stop at EXIT, controlled error + non-zero exit for missing file), both using the same `CommandProcessor`; verify with unit tests driving `App` with a temp command file and with a stream
- [x] 5.3 Document build and usage (commands, data file, execution modes) in `README.md` and verify the documented commands run as written

## 6. Integration

- [x] 6.1 Run `mvn clean package` and an end-to-end check of the JAR in file mode (the assignment's sample `input.txt` plus a line after EXIT) and in interactive mode via piped stdin, confirming expected output and `users.txt` contents across two runs
