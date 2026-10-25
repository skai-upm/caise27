# Tasks

## 1. HELP and EXIT commands

- [x] 1.1 Add `HELP` (syntax list of the three `USER` commands, `HELP`, `EXIT`) and `EXIT` (`OK EXIT` + `isFinished()`) to `CommandProcessor`; verify with `CommandProcessorTest` (help content, every documented command is recognized, EXIT sets the flag, wrong parameter counts for HELP/EXIT rejected)

## 2. Execution modes

- [x] 2.1 Add `App.start(args, stdin, out)` selecting standard input or the file of the first argument, make `run` stop when the processor is finished, and reduce `main` to delegation; verify with `AppTest` (interactive, file order, blank lines, commands after EXIT not executed, missing file message without exception)
- [x] 2.2 Verify end to end with `mvn package`: run `java -jar target/parque.jar input_basico.txt` (Ana, Pedro, UPDATE PHONE, EXIT, extra CREATE) and the interactive `HELP`/`EXIT` example in a scratch directory, and a missing file argument; check outputs and `data/users.txt`
