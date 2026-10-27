# Tasks

## 1. HELP and EXIT

- [x] 1.1 Add HELP to `CommandProcessor` returning the syntax of all available commands; verify in `CommandProcessorTest` that each required syntax line is present
- [x] 1.2 Add EXIT setting an exit flag and make `Main` stop reading after it; verify in `MainTest` that a command after EXIT is not executed in file mode and in interactive mode, with exit status 0

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F1-C08] Add help and exit commands`; verify with `git log -1 --format=%s`
