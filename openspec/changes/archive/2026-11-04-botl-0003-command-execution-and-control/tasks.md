# Tasks

## 1. General commands

- [x] 1.1 Add HELP (single `USAGE` list + editable fields) and EXIT (exit flag, confirmation) to `CommandProcessor`, rejecting parameters; verify with new `CommandProcessorTest` cases (help content, `HELP;x` error, EXIT flag)

## 2. Shared input loop

- [x] 2.1 Create `CommandRunner` that skips blank lines, prints each result and stops on EXIT or EOF; verify with `CommandRunnerTest` (blank lines, stop after EXIT, EOF, processed order)

## 3. Mode selection and file errors

- [x] 3.1 Rewrite `App` to choose stdin (no args) or the first-argument file, fix the users file to `users.txt`, and report missing/non-regular/unreadable files with one `ERROR` line and exit status 1; verify with `AppTest` (missing file, directory, file run with `input_basico.txt` content, commands after EXIT not executed)
- [x] 3.2 Verify equivalence: same command sequence through stdin and file gives identical output and users files (`AppTest`)

## 4. Regression and integration

- [x] 4.1 Run `mvn clean package` with all existing user-management tests passing, and run the JAR manually in both modes, including a missing-file run showing no stack trace
