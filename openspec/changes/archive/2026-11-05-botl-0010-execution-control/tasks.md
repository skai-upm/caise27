# Tasks

## 1. Commands

- [x] 1.1 Add `HELP` to `CommandProcessor` returning the syntax of all commands; verify a unit test that the text contains each required syntax line
- [x] 1.2 Add `EXIT` setting an exit-requested state (with `GOODBYE` response) and reject extra elements for HELP/EXIT; verify unit tests
- [x] 1.3 Stop `InteractiveSession` after a line that requested exit; verify a test that a command after EXIT is not executed

## 2. Integration

- [x] 2.1 Run the JAR with a file `USER CREATE Ana / EXIT / USER CREATE Pedro` and interactively with `HELP` then `EXIT` followed by more input; verify Pedro is not created, exit status 0; run `mvn clean package` and verify all tests pass
