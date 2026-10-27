# Tasks

## 1. File-mode behaviour

- [x] 1.1 Make `CommandProcessor.process` return no output for blank or whitespace-only lines; verify in `CommandProcessorTest`
- [x] 1.2 Add `MainTest` cases: ordered CREATE/UPDATE/DELETE file, identical stdout for the same lines via stdin and via file, blank lines ignored, continuation after each interactive command; verify `mvn package` and a manual `java -jar target/nombre-aplicacion.jar input.txt` run

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F1-C07] Add command file input`; verify with `git log -1 --format=%s`
