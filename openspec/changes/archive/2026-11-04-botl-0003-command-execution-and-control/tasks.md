# Tasks

## 1. General commands

- [x] 1.1 Add `HELP` (single `HELP_LINES` list with the five syntaxes) and `EXIT` (sets `isExitRequested()`) to `CommandProcessor`, both rejecting parameters; verify with new `CommandProcessorTest` cases for help content, `HELP;extra`, `EXIT`, `EXIT;now`

## 2. Shared loop and mode selection

- [x] 2.1 Create `CommandRunner.run(BufferedReader, PrintStream)` that skips blank lines, prints results, stops on `EXIT` or EOF and reports read errors as `ERROR`; verify with `CommandRunnerTest` (EXIT stops further commands, blank lines, EOF without EXIT, read failure)
- [x] 2.2 Refactor `App` into `run(args, stdin, out, usersFile)` with stdin/file selection and controlled file-open errors, `main` delegating to it; verify with `AppTest` (file mode in order, commands after EXIT not run, missing file, directory path, invalid UTF-8, stdin vs file equivalence of output and stored users)

## 3. Integration checks

- [x] 3.1 Run `mvn clean test` with all previous tests passing, then run the packaged JAR with the appendix `input_basico.txt` sequence and interactively via a pipe, confirming the expected output
