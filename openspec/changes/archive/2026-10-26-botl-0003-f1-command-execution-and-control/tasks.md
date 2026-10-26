# Tasks

## 1. Commands

- [x] 1.1 Add `HELP` (lists syntax of USER CREATE/DELETE/UPDATE, HELP, EXIT from one list) and `EXIT` (`OK EXIT`, sets exit flag) to `CommandProcessor`; verify with unit tests

## 2. Shared execution loop and modes

- [x] 2.1 Create `CommandRunner.run(BufferedReader)` that skips blank lines, prints each response and stops after EXIT; verify with unit tests for blank lines, ordering and commands after EXIT not executed
- [x] 2.2 Update `App` to use stdin without arguments and the first argument as command file otherwise, reporting unreadable files with `ERROR` and no stack trace; verify by running the jar interactively (piped stdin), with a command file containing blank lines and a command after EXIT, and with a missing file
- [x] 2.3 Run `mvn test` and verify all tests pass
