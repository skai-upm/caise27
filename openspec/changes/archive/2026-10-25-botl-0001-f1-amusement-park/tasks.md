# Tasks

## 1. Project setup

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `parque`) and move it to the repository root; verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `pom.xml` for Java 21 (`maven.compiler.release=21`), `finalName` `parque` and a `maven-jar-plugin` manifest with `Main-Class: es.upm.parque.App`; add `data/` to `.gitignore`; verify `mvn package` succeeds and `java -jar target/parque.jar` starts

## 2. User model and validation

- [x] 2.1 Implement `User` with encapsulated fields and an immutable DNI, and the `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with unit tests
- [x] 2.2 Implement `UserValidator` (DNI algorithm, email, password, 16-digit card) without regular expressions; verify with unit tests covering every valid/invalid example of the spec

## 3. Persistence

- [x] 3.1 Implement `UserRepository` that saves all users as `;`-separated lines (atomic replace) and loads them back, treating a missing file as empty and skipping malformed lines; verify with round-trip unit tests on a temporary file

## 4. User management rules

- [x] 4.1 Implement `UserManager` create (validation + DNI/email uniqueness) persisting after success; verify with unit tests including duplicate DNI/email
- [x] 4.2 Implement delete (email + password check) and update (credentials, enum field, revalidation, email uniqueness, DNI rejected) persisting after success; verify with unit tests including restart via a new repository instance

## 5. Commands and execution modes

- [x] 5.1 Implement `CommandProcessor` (String.split parsing, parameter-count checks, unknown commands, HELP, EXIT) delegating to `UserManager`; verify with unit tests
- [x] 5.2 Implement `App` interactive and file modes sharing `CommandProcessor` (blank lines skipped, stop at EXIT, controlled error for unreadable files); verify with unit tests using in-memory streams and temporary files
- [x] 5.3 Document usage (build, run interactively, run with a file, commands) in `README.md`; verify the documented commands run as written

## 6. Integration check

- [x] 6.1 Run `mvn test` and `mvn package`, then execute `java -jar target/parque.jar` with a sample command file and verify users persist across two runs
