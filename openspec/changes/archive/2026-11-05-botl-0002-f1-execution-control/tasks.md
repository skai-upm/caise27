# Tasks

## 1. Command processing core

- [x] 1.1 Create `CommandProcessor` (shared loop over a `BufferedReader`, blank-line skipping, keyword dispatch, `ERROR:` message for unknown commands) and verify with unit tests for blank lines and unknown commands
- [x] 1.2 Implement `HELP` listing the mandatory command syntax and verify with a unit test asserting each listed syntax line
- [x] 1.3 Implement `EXIT` stopping the loop and verify with unit tests that lines after `EXIT` are not executed and that end of input without `EXIT` terminates normally

## 2. Entry point

- [x] 2.1 Update `App.main` to run the processor on standard input (no args) or on the file given as first argument (controlled error if unreadable); replace the archetype test with tests verifying identical output for stdin and file input

## 3. Verification

- [x] 3.1 Run `mvn -q test` and verify all tests pass; run `java -cp target/classes es.upm.amusementpark.App` with a file containing `HELP`, `EXIT`, `HELP` and verify help is printed once
