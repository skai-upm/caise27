# Tasks

## 1. Command processing core

- [x] 1.1 Add `CommandResult` (output + terminate flag) and `CommandProcessor` handling `HELP` (mandated syntax list), `EXIT` (terminate) and unknown commands (error message), and verify with unit tests in `CommandProcessorTest`
- [x] 1.2 Add `CommandRunner` that reads lines from a `BufferedReader` and writes to a `PrintStream`, stopping after EXIT or at end of input, and verify with tests that a line after EXIT is never executed and that end of input terminates normally

## 2. Entry point

- [x] 2.1 Change `App.main` to run `CommandRunner` on stdin (no args) or on the file given as the first argument, with a basic error message for an unreadable file, and verify with a test that file input and stdin input of `HELP`/`EXIT` produce identical output
- [x] 2.2 Remove the obsolete archetype placeholder test, document HELP/EXIT usage in `README.md`, and verify that `mvn -q clean test` passes
