# Tasks

## 1. Project scaffolding

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.parque`, artifactId `parque`) and move `pom.xml` and `src/` to the repository root; verify the archetype layout exists
- [x] 1.2 Set `maven.compiler.release` to 21, configure `maven-jar-plugin` manifest main class `es.upm.parque.App` and `finalName` `parque`, add `data/` to `.gitignore`; verify `mvn package` succeeds and `java -jar target/parque.jar` starts

## 2. User model and validation

- [x] 2.1 Implement `User` (encapsulated fields, immutable DNI) and `UserField` enum; verify with unit tests that DNI has no setter and fields update via setters
- [x] 2.2 Implement `UserValidator` (DNI check letter, email, password, card) without regular expressions; verify with unit tests covering every valid/invalid example in the spec

## 3. Persistence

- [x] 3.1 Implement `UserRepository` with `load()`/`save()` on a configurable path using `;`-separated lines; verify a save/load round-trip test and that a missing file loads as empty

## 4. User operations

- [x] 4.1 Implement `UserManager.create` with validation and DNI/email uniqueness, persisting after success; verify with unit tests for valid, invalid and duplicate cases
- [x] 4.2 Implement `UserManager.delete` (email lookup, password check, persist); verify with unit tests for success, wrong password and unknown email
- [x] 4.3 Implement `UserManager.update` using `UserField` with credential check and revalidation (email format/uniqueness, password, card); verify with unit tests for each field and each rejection

## 5. Command processing and execution modes

- [x] 5.1 Implement `CommandProcessor` (split with `;`, dispatch USER CREATE/DELETE/UPDATE, HELP, EXIT, parameter-count checks, error handling); verify with unit tests for each command, unknown commands and missing parameters
- [x] 5.2 Implement `App` interactive mode (stdin until EXIT/EOF) and file mode (first argument, line by line, blank lines ignored, stop at EXIT, controlled error for missing/unreadable file) sharing `CommandProcessor`; verify with tests running a temp command file, including commands after EXIT and a nonexistent file
- [x] 5.3 Document usage (build, run modes, commands) in README.md; verify the documented commands work as written

## 6. Integration

- [x] 6.1 Run `mvn test` and `mvn package`, then execute the JAR interactively and with a command file, restarting to confirm users are restored; verify all outputs match the specs
