# Tasks

Stages are cumulative: Bootstrap (1) → User management (2–4) → Execution and control (5–6). Each stage must leave `mvn test` passing and preserve the behavior of the previous stage.

## 1. Bootstrap (Stage 1)

- [x] 1.1 Generate the Maven Quickstart project at the repository root (`groupId=es.upm.amusementpark`, `artifactId=amusement-park`, package `es.upm.amusementpark`) with `maven.compiler.release=21` and JUnit Jupiter in `test` scope; verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Configure `maven-jar-plugin` manifest `mainClass=es.upm.amusementpark.App`; verify `mvn package` produces a JAR whose `java -jar` launch runs `App.main`
- [x] 1.3 Add `users.txt` to `.gitignore` (`target/` is already ignored) so runtime data is not committed; verify `git status` shows no build output or data file after a build and run
- [x] 1.4 Keep a minimal `AppTest` with no domain logic; verify `mvn test` passes

## 2. User model and validation (Stage 2)

- [x] 2.1 Implement `User` with private fields, getters, setters for editable fields and a `final` DNI with no setter, plus `UserField` enum (`NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`); verify with `UserTest`
- [x] 2.2 Implement `UserValidator` (DNI checksum, email, password, card, non-blank) without regex; verify with `UserValidatorTest` covering every valid/invalid example from the specs

## 3. User persistence (Stage 2)

- [x] 3.1 Implement `UserRepository(Path)` with `loadAll()` (missing file → empty, skip blank/malformed lines) and `saveAll(...)` writing `;`-separated lines in spec order; verify with `UserRepositoryTest` round-trip and exact line-format tests using a temp directory

## 4. User operations and commands (Stage 2)

- [x] 4.1 Implement `UserManager` (load at construction, create with validation + DNI/email uniqueness, delete with authentication, update with authentication + revalidation + email uniqueness, save after each success, rollback on save failure); verify with `UserManagerTest` covering every user-management scenario including restart persistence
- [x] 4.2 Implement `CommandProcessor` parsing with `split(";", -1)`, exact parameter counts, and dispatch of `USER CREATE/DELETE/UPDATE` with `OK:`/`ERROR:` output; verify with `CommandProcessorTest` for valid commands, missing parameters, unknown commands and unknown/DNI update fields
- [x] 4.3 Wire `App` to build `UserManager`/`CommandProcessor` on `users.txt`; verify `mvn test` passes and the Stage 1 tests still pass

## 5. Execution modes, HELP and EXIT (Stage 3)

- [x] 5.1 Add `HELP` (lists the five commands with syntax) and `EXIT` (stops processing) to `CommandProcessor`; verify with tests asserting the HELP text and the continue/stop return value
- [x] 5.2 Implement `App.run(args, in, out, err, dataFile)`: interactive mode on stdin until `EXIT`/EOF, file mode on `args[0]` line by line skipping blank lines and stopping at `EXIT`, both through the same `CommandProcessor`; verify with `AppTest` for interactive session, end-of-input, file with blank lines, and commands after `EXIT` not executed
- [x] 5.3 Handle nonexistent/unreadable files with a controlled `ERROR:` message and no stack trace; verify with an `AppTest` case using a missing path
- [x] 5.4 Add a mode-equivalence test feeding the same command sequence via stdin and via file with fresh data files; verify identical output and stored users

## 6. Integration verification

- [x] 6.1 Run `mvn test` and `mvn package`; launch the JAR with a sample command file (create, blank line, update, HELP, EXIT, create) and interactively via piped stdin, and verify output, `users.txt` contents, and that a restart restores the stored users
