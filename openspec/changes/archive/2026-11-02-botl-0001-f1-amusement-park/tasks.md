# Tasks

## 1. Bootstrap (Maven quickstart on JDK 21)

- [x] 1.1 Generate the project at the repository root with `maven-archetype-quickstart` (groupId `com.amusementpark`, artifactId `amusement-park`, package `com.amusementpark`) and verify `pom.xml`, `src/main/java/com/amusementpark/App.java` and `src/test/java/com/amusementpark/AppTest.java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` passes
- [x] 1.3 Configure the `maven-jar-plugin` manifest `mainClass` and verify `mvn package` produces a JAR that starts with `java -jar` (no domain functionality yet)

## 2. User management

- [x] 2.1 Implement `User` (encapsulated fields, immutable `dni`) and the `UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); verify with unit tests that there is no DNI setter path and getters/setters work
- [x] 2.2 Implement `UserValidator` (DNI checksum, email, password, card, non-empty) without regular expressions; verify with unit tests covering every valid/invalid example in the spec
- [x] 2.3 Implement `UserFileStorage` (save all users as semicolon-separated lines, load at startup, missing file → empty, malformed lines skipped); verify with round-trip unit tests using a temporary file
- [x] 2.4 Implement `UserManager` create/delete/update with validation, DNI/email uniqueness, credential checks, persistence after each success and rollback on save failure; verify with unit tests for success, rejection, uniqueness, wrong credentials, non-editable DNI and restart persistence
- [x] 2.5 Implement `USER CREATE`, `USER DELETE` and `USER UPDATE` parsing/dispatch in `CommandProcessor` using `String.split(";", -1)` with parameter-count checks; verify with tests that valid commands succeed and malformed/insufficient commands print `ERROR:` without changing state, then run `mvn test`

## 3. Execution and control

- [x] 3.1 Add the shared `CommandProcessor.run(BufferedReader)` loop (blank lines ignored, stops on `EXIT` or end of input) and the `HELP` and `EXIT` commands; verify with tests that HELP lists all five commands and that commands after `EXIT` are not executed
- [x] 3.2 Wire `App.main`: no arguments → standard input, first argument → command file; report nonexistent/unreadable files with an `ERROR:` message and no stack trace; verify with tests for file mode, invalid file, and equivalence of interactive vs. file output for the same command sequence
- [x] 3.3 Integration check: run `mvn test` and `mvn package`, then run the JAR in file mode with a sample command file (including blank lines and a command after `EXIT`) and interactively via piped stdin, and confirm the users file is reloaded on a second run
