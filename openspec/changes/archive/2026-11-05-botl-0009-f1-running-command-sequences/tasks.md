# Tasks

## 1. File input

- [x] 1.1 Make `App.main` read commands from the file named by `args[0]` (UTF-8, try-with-resources) through the existing `run`, and from standard input otherwise; verify with an `AppTest` using a temp command file whose ordered commands (with a blank line) produce ordered OK responses
- [x] 1.2 Handle a nonexistent or unreadable (directory) command file by printing `ERROR: cannot read command file '<path>'` and returning without exception or data changes; verify with `AppTest` cases for both

## 2. Integration

- [x] 2.1 End-to-end in a temp directory with the JAR: run the §10.2 example file (with `EXIT` answered as unknown for now) and verify ordered responses and resulting users; pipe the same lines through stdin in a fresh directory and verify identical output; run with `missing.txt` and verify the ERROR line, no stack trace, exit status 0
