# Tasks

## 1. F0-C01 — Project initialization

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.etsisi`, artifactId `amusement-park`, package `es.upm.etsisi.park`) into the repository root, keeping the standard layout; verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Set `maven.compiler.release` to `21` and UTF-8 encoding in `pom.xml`; verify `mvn test` compiles and the baseline test passes

## 2. F0-C02 — Executable application

- [x] 2.1 Configure `<finalName>amusement-park</finalName>` and the `maven-jar-plugin` manifest `mainClass` `es.upm.etsisi.park.App`; verify `mvn package` produces `target/amusement-park.jar` and `java -jar target/amusement-park.jar` starts and reads standard input
- [x] 2.2 Make `App` choose interactive mode (no args) or file mode (first arg) through one line-processing entry point; verify with a test that both modes reach the same processing method

## 3. F1-C01 — User model

- [x] 3.1 Implement `User` with private fields for the seven attributes, getters, setters for all but DNI, and a `final` DNI; verify with `UserTest` that all attributes read back and that no DNI setter exists (reflection check)

## 4. F1-C02 — User data validation

- [x] 4.1 Implement `UserValidator` DNI check (8 digits + correct check letter); verify `UserValidatorTest` accepts `12345678Z` and rejects `12345678A` and malformed lengths/characters
- [x] 4.2 Implement email check (`text@text.xxx`, split on `@`, three-character extension); verify the test accepts `ana@correo.com` and rejects `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es`
- [x] 4.3 Implement password check (upper, lower, digit, symbol); verify the test accepts `Clave1!` and rejects `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12`
- [x] 4.4 Implement card check (exactly 16 digits); verify the test accepts `1234567890123456` and rejects `1234 5678 9012 3456`, `123456789012345`, `12345678901234567`
- [x] 4.5 Verify no regular expressions are used in `src/main/java` (`grep` for `matches(`, `replaceAll(`, `java.util.regex`, `Pattern` returns nothing)

## 5. F1-C03 — User creation

- [x] 5.1 Implement `UserService.create` validating all fields and checking DNI and email uniqueness before inserting; verify `UserServiceTest` covers success, invalid DNI/email/password/card, duplicate DNI and duplicate email (user count unchanged)
- [x] 5.2 Implement `CommandProcessor` with `split(";", -1)`, keyword dispatch, parameter-count checks, `USER CREATE` → `USER CREATED`, and `ERROR: ...` for unknown/malformed commands; verify `CommandProcessorTest` covers creation, wrong parameter count and unknown command without exceptions

## 6. F1-C04 — User persistence

- [x] 6.1 Implement `UserRepository` (load/saveAll; one `;`-separated line per user in the specified attribute order; missing file = empty; malformed lines skipped with a warning; temp-file + move write); verify `UserRepositoryTest` round-trips users and checks the exact line format
- [x] 6.2 Wire `UserService` to load on start and save after each successful mutation, rolling back memory on write failure; verify a test that creates a user, builds a new service on the same file and finds the user (and the duplicate check still applies); verify `App`/`User` contain no file I/O for user data

## 7. F1-C05 — User deletion

- [x] 7.1 Implement `UserService.delete(email, password)` and `USER DELETE` → `USER DELETED`; verify tests for correct credentials (removed from memory and file), wrong password and unknown email (state and file unchanged)

## 8. F1-C06 — User update

- [x] 8.1 Add `UserField` enum and `UserService.update(email, password, field, value)` with credential check, per-field validation, email uniqueness (excluding the same user), persistence and rollback; verify tests for NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD success and for DNI/unknown field, wrong password, duplicate email, invalid email/password/card (user unchanged)
- [x] 8.2 Wire `USER UPDATE` → `USER UPDATED` in `CommandProcessor`; verify the command-level test and that an updated email persists across a reload

## 9. F1-C07 — Command file input

- [x] 9.1 Implement `CommandRunner` reading a `BufferedReader` line by line, skipping blank lines, processing in order through the shared `CommandProcessor`; verify `CommandRunnerTest` runs the same sequence from a string reader (interactive) and from a temp file and gets identical output and state
- [x] 9.2 Handle nonexistent/unreadable command files with `ERROR: cannot read file <path>` and a non-zero exit status without stack trace; verify with an `App` test

## 10. F1-C08 — HELP and EXIT

- [x] 10.1 Implement `HELP` printing the five command syntax lines and `EXIT` stopping the runner; verify tests that HELP lists all commands and that a command after `EXIT` in a file is not executed

## 11. F1-C09 — Delivery 1 integration

- [x] 11.1 Run `mvn package` from clean, then run `java -jar target/amusement-park.jar` with the assignment's example `input.txt` (isolated data file) and verify output and resulting data file (only Ana, phone `699999999`, email `ana.nueva@correo.com`)
- [x] 11.2 Run a mixed valid/invalid command file through the JAR and an interactive session via piped stdin; verify all errors are reported, valid commands persist and the process exits normally
- [x] 11.3 Verify `pom.xml` has no non-test dependencies and `src/main/java` uses no regex or encryption APIs
