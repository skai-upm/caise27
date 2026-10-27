# Tasks

## 1. Shared command processing

- [x] 1.1 Create `CommandProcessor` (process one line, return whether to continue; HELP, EXIT, blank-line skip, unknown-command error) and verify with `CommandProcessorTest` covering HELP content, EXIT result and unknown command
- [x] 1.2 Create `CommandRunner` that feeds lines from a `BufferedReader` to the processor until EXIT or end of input, and verify with `CommandRunnerTest` that a command after `EXIT` is not processed and that input without EXIT ends normally

## 2. Entry point

- [x] 2.1 Update `App.main` to read from standard input without arguments or from the file given as the first argument (controlled message if it cannot be opened), and verify with `AppTest` that stdin and file input produce the same HELP output and that file lines after `EXIT` are ignored
- [x] 2.2 Run `mvn package` and verify the build and all tests pass
