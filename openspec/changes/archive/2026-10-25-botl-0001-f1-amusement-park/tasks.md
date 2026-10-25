# Tasks

## 1. Project setup

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `com.amusementpark`, artifactId `amusement-park`) and move `pom.xml` and `src/` to the repository root; verify the standard layout exists and `mvn test` passes on the untouched archetype output
- [x] 1.2 Set `maven.compiler.release` to 21 and configure the `maven-jar-plugin` manifest `mainClass` as `com.amusementpark.App`; verify `mvn package` succeeds and `java -jar target/amusement-park-*.jar` launches the main class
- [x] 1.3 Add `target/`-style runtime artifacts (`users.txt`) to `.gitignore`; verify `git status` does not show the users file after running the app

## 2. User model and validation

- [x] 2.1 Implement `User` with private fields, getters, setters for every field except a `final` DNI; verify with a unit test that fields round-trip and no DNI setter exists
- [x] 2.2 Implement `UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) with an exception-free lookup by name; verify with a unit test that `DNI` and unknown names return no field
- [x] 2.3 Implement `Validator` DNI check (8 digits + modulo-23 letter) without regex; verify unit tests accept `12345678Z` and reject `12345678A` and malformed values
- [x] 2.4 Implement `Validator` email check using `split`/`String` operations; verify unit tests accept `ana@correo.com` and reject `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es`
- [x] 2.5 Implement `Validator` password check; verify unit tests accept `Clave1!` and reject `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12`
- [x] 2.6 Implement `Validator` card check (exactly 16 digits); verify unit tests accept `1234567890123456` and reject `1234 5678 9012 3456`, 15 and 17 digit values

## 3. Persistence

- [x] 3.1 Implement `UserRepository.save` writing one `;`-separated line per user in field order; verify a unit test with a temp file checks the exact line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- [x] 3.2 Implement `UserRepository.load` returning an empty list for a missing file and skipping malformed lines; verify unit tests for round-trip, missing file and malformed lines

## 4. User management operations

- [x] 4.1 Implement `UserManager` create with full validation and DNI/email uniqueness, persisting on success; verify unit tests for success, each invalid field, duplicate DNI and duplicate email
- [x] 4.2 Implement `UserManager` delete by email + password, persisting on success; verify unit tests for success (file updated), unknown email and wrong password
- [x] 4.3 Implement `UserManager` update by email + password + field + value, revalidating email (format and uniqueness against other users), password and card, rejecting DNI/unknown fields; verify unit tests for each field example in the spec and each rejection case
- [x] 4.4 Load existing users from `UserRepository` when `UserManager` is constructed; verify a unit test that a user saved by one instance is found (uniqueness and credentials apply) by a new instance

## 5. Command interface

- [x] 5.1 Implement `CommandProcessor.process(line)` splitting with `String.split(";", -1)`, ignoring blank lines, dispatching `USER CREATE`/`USER DELETE`/`USER UPDATE` with arity checks and reporting unknown commands; verify unit tests for each command, unknown command, insufficient parameters and blank lines
- [x] 5.2 Implement `HELP` listing all command syntaxes and `EXIT` returning stop; verify unit tests that HELP output contains each required syntax and EXIT returns false
- [x] 5.3 Implement `App.main` choosing stdin (no args) or the file in `args[0]`, feeding lines to the shared `CommandProcessor` until EXIT or end of input, and reporting nonexistent/unreadable files without a stack trace; verify unit tests that run `App` logic against a temp commands file (commands after EXIT not executed, blank lines ignored, missing file reported) and against a simulated stdin

## 6. Integration check

- [x] 6.1 Run `mvn test` and `mvn package`, then run the JAR in file mode with a sample `input_basico.txt` and in interactive mode with piped `HELP`/`EXIT`, confirming `users.txt` contents and that a second run reloads the users
