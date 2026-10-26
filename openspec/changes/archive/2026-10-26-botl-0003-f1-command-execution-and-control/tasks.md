# Tasks

## 1. HELP and EXIT commands

- [x] 1.1 Add `HELP` to `CommandProcessor`, printing the syntax of USER CREATE/DELETE/UPDATE, HELP and EXIT; verify a unit test checks every syntax line is present
- [x] 1.2 Add `EXIT` with an `isExitRequested()` flag and reject parameters on HELP/EXIT; verify unit tests for the flag and for `HELP;x`

## 2. Execution modes

- [x] 2.1 Refactor `App` into `run(args, stdin, out, usersFile)` with a single shared line loop that skips blank lines and stops at EXIT; verify an interactive-mode test where commands after EXIT are not executed
- [x] 2.2 Add file mode using the first argument as the path; verify a test that runs a temp command file (with blank lines) and that commands after EXIT are not executed
- [x] 2.3 Handle nonexistent/unreadable files with a controlled `ERROR` message; verify a test with a missing path produces the message and no exception
- [x] 2.4 Verify both modes give identical output for the same commands (test), and run `mvn test` to verify all existing user-management tests still pass
