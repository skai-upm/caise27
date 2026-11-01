# Tasks

## 1. Bootstrap (no domain features)

- [x] 1.1 Generate the project from `org.apache.maven.archetypes:maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`) at the repository root, keeping its standard layout; verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Set the compiler release to 21 and configure `maven-jar-plugin` with manifest main class `es.upm.amusementpark.App`; verify `mvn test` passes
- [x] 1.3 Run `mvn package` and verify `java -jar target/amusement-park-*.jar` starts the application without error

## 2. User management (depends on 1)

- [x] 2.1 Implement `User` with encapsulated fields and a `final` DNI without setter, plus `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with unit tests that the enum has no DNI entry and getters return constructor values
- [x] 2.2 Implement `UserValidator` (DNI checksum, email, password, card) without regular expressions; verify unit tests cover every valid and invalid example from the specs
- [x] 2.3 Implement `UserStorage` (`load`/`save`, semicolon format, missing file = empty list, malformed lines skipped); verify a round-trip test in a temporary directory reproduces the users and the exact line format
- [x] 2.4 Implement `UserManager.create` with full validation and DNI/email uniqueness, saving only on success; verify tests for valid creation, each invalid field, duplicate DNI, duplicate email, and unchanged file on rejection
- [x] 2.5 Implement `UserManager.delete` (email lookup + password check) and `UserManager.update` (credential check, field parsing, revalidation, email uniqueness, rollback on save failure); verify tests for authenticated delete/update, wrong credentials, unknown field and `DNI` field rejection, invalid new password/card/email, email taken by another user
- [x] 2.6 Verify persistence across restarts: test that a new `UserManager` over the same file sees users after create, update and delete; run `mvn test`

## 3. Execution and control (depends on 2)

- [x] 3.1 Implement `CommandProcessor.process` parsing with `String.split(";", -1)`, exact parameter counts, dispatch of `USER CREATE/DELETE/UPDATE`, `HELP` text listing the five commands, `EXIT` returning false, and error messages for unknown/malformed commands; verify unit tests for each command, HELP output, and unknown/insufficient-parameter commands not throwing
- [x] 3.2 Implement `CommandProcessor.run(BufferedReader)` that skips blank lines and stops at `EXIT` or end-of-input; verify tests that commands after `EXIT` are not executed and blank lines cause no error
- [x] 3.3 Implement `App.main` mode selection (no args → stdin, first arg → file) using the shared `run`, with controlled error message for missing/unreadable files and `users.txt` as the data file; verify tests that a missing file prints an error without exception and that the same command sequence via stdin and via file produces identical output
- [x] 3.4 Integration check: run `mvn package`, then run the JAR with a sample command file (create, blank line, update, HELP, EXIT, create) and interactively via piped stdin; verify output, `users.txt` contents, and that the post-EXIT command was not executed; confirm `mvn test` passes
