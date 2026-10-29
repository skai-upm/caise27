# Tasks

## 1. Command processing core

- [x] 1.1 Create `CommandProcessor` with an injected `PrintStream`, `execute(String line)` (trim, ignore blank, reject unknown with an error message) and `run(BufferedReader)` that stops at end of input or when `execute` returns false; verify with unit tests for unknown-command rejection and continued processing
- [x] 1.2 Implement `HELP` printing the syntaxes `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`; verify with a unit test asserting each syntax is printed and that processing continues after HELP
- [x] 1.3 Implement `EXIT` returning false so no later line is processed; verify with a unit test feeding `HELP`, `EXIT`, `HELP` and asserting help is printed only once

## 2. Input sources

- [x] 2.1 Update `App.main` to run the processor over standard input without arguments and over the file given as first argument otherwise, printing a controlled error for an unreadable file; replace the quickstart sample test and verify with tests that run `App.main` with a temporary file and with redirected stdin, asserting identical output and that lines after EXIT are not executed
- [x] 2.2 Run `mvn package` and verify `BUILD SUCCESS` with all tests passing
