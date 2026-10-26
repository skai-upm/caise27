# Tasks

## 1. HELP and EXIT commands

- [x] 1.1 Add single-word operation handling plus `HELP` (usage list shared with parameter-count errors) and `EXIT` (exit flag) to `CommandProcessor`; verify with `CommandProcessorTest` cases for HELP content, HELP/EXIT with extra parameters and the exit flag

## 2. Shared execution loop and modes

- [x] 2.1 Add `CommandProcessor.run(BufferedReader, PrintStream)` that skips blank lines and stops at EXIT or EOF; verify with tests showing commands after EXIT are not executed and blank lines are ignored
- [x] 2.2 Make `App.main` use standard input without arguments and the first argument as command file otherwise, reporting unreadable/nonexistent files without a stack trace; verify with `AppTest` (file mode executes in order, nonexistent file prints an error) and by running `java -jar target/parque.jar input.txt`
- [x] 2.3 Run `mvn test` and verify all previous and new tests pass
