# Tasks

## 1. Commands

- [x] 1.1 Add `HELP` to `CommandProcessor` printing the syntax of all available commands; verify unit test checking each required syntax line
- [x] 1.2 Add `EXIT` setting an exit flag and returning `Goodbye`, and stop `App.run` when the flag is set; verify unit tests for the flag and an `App` test where a command after `EXIT` in a file and on stdin is not executed
- [x] 1.3 Run `mvn package` and the JAR with a file containing create/EXIT/create, and interactively with `HELP` then `EXIT` on an open stdin; verify `BUILD SUCCESS`, only the first user stored and exit status 0

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C08] Add help and exit commands`; verify with `git log -1 --format=%s`
