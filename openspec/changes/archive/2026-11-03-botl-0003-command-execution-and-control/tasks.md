# Tasks

## 1. General commands

- [x] 1.1 Add `HELP` (syntax of USER CREATE/DELETE/UPDATE, HELP, EXIT from one constant list) and `EXIT` (sets an exit flag, returns `OK: Bye`) to `CommandProcessor`, rejecting extra parameters; verify `CommandProcessorTest` checks help content, that HELP changes no data, and the exit flag

## 2. Input modes

- [x] 2.1 Refactor `App` so that `main` selects standard input (no args) or the file in `args[0]`, and both feed one `run(BufferedReader, PrintStream, Path)` loop that skips blank lines and stops on EXIT or EOF; verify `AppTest` covers the interactive session example, EOF without EXIT and that lines after EXIT are not processed
- [x] 2.2 Handle missing, directory, unreadable and undecodable command files with an `ERROR:` message and no uncaught exception; verify `AppTest` cases for a missing path and a directory path, and that `main` returns normally

## 3. Integration verification

- [x] 3.1 Verify equivalence and regression: a test runs the same command sequence (with blank lines) via standard input and via a file over separate empty user files and asserts identical output and user files; then `mvn clean test` passes and `java -jar target/amusement-park-1.0-SNAPSHOT.jar input_basico.txt` processes the sample file
