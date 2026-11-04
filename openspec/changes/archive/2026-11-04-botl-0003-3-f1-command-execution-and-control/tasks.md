# Tasks

## 1. General commands

- [x] 1.1 Add the `terminate` flag and `Result.exit` to `Result`, and `HELP`/`EXIT` handling (no parameters allowed, help lists all five command syntaxes) to `CommandProcessor`; verify with `CommandProcessorTest` cases for HELP content, HELP/EXIT with parameters, and EXIT returning a terminating result

## 2. Shared reading loop and input modes

- [x] 2.1 Implement `CommandRunner` (skip blank lines, print one result per command, stop at EXIT or end of input); verify with `CommandRunnerTest` for ordering, blank lines, EOF and nothing executed after EXIT
- [x] 2.2 Refactor `App` into `run(args, stdin, out, err)` selecting stdin (no args) or the first argument as a command file, with controlled errors and exit status; verify with `AppTest` for interactive session, file run, missing file, directory path (no stack trace, nonzero status) and stdin/file equivalence of output and persisted users

## 3. Regression

- [x] 3.1 Verify `mvn test` passes with all previous user-management tests unchanged, and run the packaged JAR with a command file (`input_basico.txt` example) and interactively to confirm EXIT and HELP behavior
