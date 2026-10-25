# Tasks

## 1. Project setup

- [x] 1.1 Generate the project with `mvn archetype:generate` (maven-archetype-quickstart 1.5, groupId `es.upm.parque`, artifactId `parque`) and move it to the repository root; verify `pom.xml`, `src/main/java/es/upm/parque/App.java` and `src/test/java/es/upm/parque/AppTest.java` exist
- [x] 1.2 Configure `pom.xml` for Java 21 (`maven.compiler.release=21`), `finalName` `parque` and a `Main-Class` manifest entry; verify `mvn package` succeeds and `java -jar target/parque.jar` runs
- [x] 1.3 Add `target/` and `data/` handling to `.gitignore` (runtime data not committed); verify `git status` does not show build output

## 2. User model and validation

- [x] 2.1 Implement `User` with encapsulated fields and immutable DNI, and `UserField` enum; verify with a unit test that getters/setters work and DNI has no setter
- [x] 2.2 Implement `UserValidator` (DNI algorithm, email without regex, password, card, non-blank); verify with unit tests covering every valid/invalid example in the user-management spec

## 3. Persistence

- [x] 3.1 Implement `UserRepository` load/saveAll for the `;`-separated file, skipping malformed lines with a warning; verify with a unit test using a temporary directory (round trip, exact line format, malformed line skipped, missing file = empty list)

## 4. User operations

- [x] 4.1 Implement `UserManager.create` with validation and DNI/email uniqueness, persisting on success; verify with unit tests (valid create, each invalid field, duplicate DNI, duplicate email, reload after restart)
- [x] 4.2 Implement `UserManager.delete` with email/password authentication; verify with unit tests (success persisted, wrong password, unknown email)
- [x] 4.3 Implement `UserManager.update` for all `UserField` values with re-validation and email uniqueness; verify with unit tests (each field, the spec's email→password→card sequence, invalid values, other user's email, wrong credentials)

## 5. Command interface

- [x] 5.1 Implement `CommandProcessor` with `String.split` parsing for `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`, unknown commands and wrong parameter counts; verify with unit tests for each command and error case
- [x] 5.2 Implement `App.main` with interactive mode (stdin) and file mode (`args[0]`) sharing one loop, blank lines ignored, stop on `EXIT`, controlled error for missing/unreadable file; verify with tests running the loop over a file and over a stdin-like reader, including commands after `EXIT` not executed and nonexistent file message
- [x] 5.3 Add `input_basico.txt` example command file and document usage in `README.md`; verify by running `java -jar target/parque.jar input_basico.txt` from a temporary working directory

## 6. Integration

- [x] 6.1 Run `mvn test` and `mvn package` and confirm both succeed with all tests passing
