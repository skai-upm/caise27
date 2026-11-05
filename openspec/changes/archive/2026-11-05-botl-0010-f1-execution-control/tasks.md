# Tasks

## 1. Commands

- [x] 1.1 Implement `HELP` in `CommandProcessor` returning the help text with the five command syntaxes (and update fields); verify a test that the response contains each syntax and that `HELP;x` is an ERROR
- [x] 1.2 Implement `EXIT` returning `OK: exiting application` and setting `isExitRequested()`; verify tests for the flag and that `EXIT;x` is an ERROR without setting it

## 2. Loop

- [x] 2.1 Make `App.run` stop after a response when exit is requested; verify `AppTest` that, for both a command file and stdin, `USER CREATE` Ana / `EXIT` / `USER CREATE` Pedro creates only Ana and prints identical output with nothing after the EXIT response

## 3. Integration

- [x] 3.1 End-to-end with the JAR in a temp directory: `HELP` output in interactive and file mode, the §12 file example (second create not executed, exit status 0), and restart shows the first user persisted
