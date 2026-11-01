# Tasks

Stages are cumulative: Bootstrap (1) → User Management (2–4) → Execution and Control (5). Each group must leave `mvn test` passing and preserve the behaviour of earlier groups.

## 1. Bootstrap (Maven Quickstart, JDK 21)

- [x] 1.1 Generate the project with `maven-archetype-quickstart` (groupId `es.upm.amusementpark`, artifactId `amusement-park`, package `es.upm.amusementpark`) at the repository root and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 and configure the `maven-jar-plugin` manifest `mainClass` to `es.upm.amusementpark.App`; verify `mvn test` passes
- [x] 1.3 Verify `mvn package` produces a JAR that `java -jar` launches without classpath arguments

## 2. User model and validation

- [x] 2.1 Implement the encapsulated `User` class with a final, setter-less DNI and the `UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); verify with unit tests that DNI has no mutator
- [x] 2.2 Implement `UserValidator` (DNI checksum, email, password, card, nonempty) with String/Character operations only (no regex); verify with unit tests covering every valid/invalid example from the specs

## 3. User persistence

- [x] 3.1 Implement `UserRepository` that loads/saves users in the `;`-separated, one-user-per-line format (missing file → empty list, malformed lines skipped); verify with round-trip tests in a temporary directory, including the exact line format from the spec

## 4. User operations and commands

- [x] 4.1 Implement `UserManager` create/delete/update with DNI/email uniqueness, credential checks, revalidation on update, persistence after each success and revert on save failure; verify with unit tests for success, duplicates, wrong credentials, invalid updates and restart persistence
- [x] 4.2 Implement `CommandProcessor` parsing `USER CREATE`, `USER DELETE`, `USER UPDATE` with `String.split(";", -1)`, exact parameter counts, trimmed and nonempty parameters, and `OK:`/`ERROR:` output; verify with command-level tests including insufficient/empty parameters, unknown fields (e.g. `DNI`) and the chained update example

## 5. Execution and control (interactive/file modes, HELP, EXIT)

- [x] 5.1 Add `HELP` (lists all five commands with syntax) and `EXIT` (signals stop) to `CommandProcessor`; verify with tests that HELP output contains each syntax line and EXIT returns a stop signal
- [x] 5.2 Add the shared read loop `CommandProcessor.run(BufferedReader, PrintStream)` that skips blank lines, stops on `EXIT`, and survives invalid commands; verify with tests for ordered processing, blank lines, no execution after EXIT, and recovery after unknown commands
- [x] 5.3 Implement `App.main` mode selection (no args → stdin, first arg → command file) with controlled error message and exit status for nonexistent/unreadable files; verify with tests that interactive and file inputs produce identical output and that a missing file yields an `ERROR:` message without a stack trace
- [x] 5.4 Integration check: run `mvn package`, then run the JAR with a command file (create, blank line, update, HELP, EXIT, create) and interactively via piped stdin, then restart and confirm users were restored from `users.txt`
