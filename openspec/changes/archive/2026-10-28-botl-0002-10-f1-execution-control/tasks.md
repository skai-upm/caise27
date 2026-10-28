# Tasks

## 1. Command processor

- [x] 1.1 Create `Command` abstraction and `CommandProcessor` that reads lines from a `BufferedReader`, skips blank lines, dispatches by keyword and reports unknown commands; verify with unit tests for unknown and blank lines
- [x] 1.2 Implement `HELP` printing the mandated syntax lines; verify with a unit test asserting every mandated line is printed and that HELP does not stop processing
- [x] 1.3 Implement `EXIT` stopping processing so no later line is executed; verify with a unit test over `HELP`, `EXIT`, `HELP` printing help exactly once
- [x] 1.4 Verify source independence with a unit test reading the same lines from a temporary file and from an in-memory stream and comparing outputs

## 2. Entry point and delivery

- [x] 2.1 Make `App.main` run the processor on standard input; verify by piping `HELP` and `EXIT` into `java -cp target/classes ...App` and observing normal termination
- [x] 2.2 Run `mvn package` (BUILD SUCCESS) and commit with the exact message `[F1-C08] Add help and exit commands`; verify with `git log -1 --format=%s`
