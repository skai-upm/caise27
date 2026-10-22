# Tasks

## 1. Project setup

- [x] 1.1 Create `pom.xml` (quickstart layout, `maven.compiler.release=21`, JUnit 5 test scope, `finalName` `amusement-park`, `maven-jar-plugin` manifest `Main-Class`) and verify `mvn package` produces `target/amusement-park.jar`
- [x] 1.2 Add `.gitignore` entry for the runtime `users.txt` data file and verify `git status` does not show it after a run

## 2. User model and validation

- [x] 2.1 Implement `model.User` (private fields, no DNI setter) and `model.UserField` enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) with case-insensitive lookup; verify with unit tests
- [x] 2.2 Implement `validation.UserValidator` (DNI check letter, email, password, card, non-empty text) without regex; verify with unit tests covering every valid/invalid example in the user-validation spec

## 3. Persistence

- [x] 3.1 Implement `persistence.UserFileStore` (load/save `;`-separated lines, missing file = empty, skip malformed lines, write via temp file + move); verify with unit tests using a temp directory
- [x] 3.2 Implement `repository.UserRepository` (in-memory list, lookup by DNI/email, add/remove/save) backed by the store; verify reload-after-save in a unit test

## 4. User operations

- [x] 4.1 Implement `service.UserService.create` with validation and DNI/email uniqueness; verify success, invalid data and duplicate scenarios with unit tests
- [x] 4.2 Implement `UserService.delete` with email lookup and password check; verify success, wrong password and unknown user with unit tests
- [x] 4.3 Implement `UserService.update` for each `UserField` with re-validation, email uniqueness and rejection of `DNI`/unknown fields; verify with unit tests

## 5. Command interface and runtime

- [x] 5.1 Implement `cli.CommandProcessor` (split on `;`, case-insensitive command names, argument-count checks, `HELP`, `EXIT`, `ERROR:` messages, never throws); verify with unit tests including unknown and malformed commands
- [x] 5.2 Implement `cli.CommandRunner` (blank lines skipped, stop on `EXIT` or EOF) and verify commands after `EXIT` are not executed
- [x] 5.3 Implement `App` main (interactive without args, file mode with first arg, controlled error for missing/unreadable file); verify by running the JAR with the assignment's sample `input.txt` and with a nonexistent file
- [x] 5.4 Document build and usage in `README.md` and verify the documented commands run as written

## 6. Integration

- [x] 6.1 Run `mvn package` from scratch and an end-to-end JAR run of the assignment example sequence twice (persistence across runs); verify all tests pass and outputs match the specs
