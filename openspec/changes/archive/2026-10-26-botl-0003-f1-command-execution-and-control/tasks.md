# Tasks

## 1. HELP and EXIT commands

- [x] 1.1 Add `COMMAND_SYNTAX` constants to `CommandProcessor`, reuse them in parameter-count errors, and implement `HELP` (`OK Available commands:` + one syntax per line); verify `CommandProcessorTest` checks all five syntaxes are listed and `HELP;x` is rejected
- [x] 1.2 Implement `EXIT` (`OK EXIT`, sets `isExitRequested()`); verify `CommandProcessorTest` checks the flag is false before and true after EXIT, and `EXIT;x` is rejected

## 2. Execution modes

- [x] 2.1 Make `App.run` stop after a command that requests exit; verify `AppTest` runs `HELP`, `EXIT`, `USER CREATE` and asserts the last command is not executed
- [x] 2.2 Implement mode selection in `App` (no args → stdin, first arg → file read line by line with the same `run`) and controlled errors for missing/unreadable files; verify `AppTest` covers file mode in order, blank lines, EXIT in a file, a missing file, a directory path, and identical results for the same commands from stdin and from a file
- [x] 2.3 Run `mvn package` and end-to-end checks with `java -jar target/parque.jar` (interactive via pipe with EXIT, file mode with blank lines and commands after EXIT, nonexistent file); verify outputs and absence of stack traces
