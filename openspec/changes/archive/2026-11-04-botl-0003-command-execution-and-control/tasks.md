# Tasks

## 1. General commands

- [x] 1.1 Add `HELP` (syntax list from a single `COMMAND_SYNTAX` list) and `EXIT` (`OK: Exiting`, exit flag) to `CommandProcessor`, rejecting extra parameters; verify `CommandProcessorTest` checks the five syntax lines, the exit flag and `HELP;extra`/`EXIT;extra` errors

## 2. Input modes

- [x] 2.1 Refactor `App` into `run(args, stdin, out, err, usersFile)` returning an exit status, with one shared line loop used for stdin and for the file in `args[0]`, skipping blank lines and stopping on `EXIT`; verify `AppTest` interactive session (create, update phone, HELP, EXIT with trailing input ignored) and file run in order
- [x] 2.2 Handle missing/unreadable command files (and a failing user-file load) with an `ERROR:` message on stderr and status 1; verify `AppTest` for a missing file and a directory path, asserting no stack-trace text is printed

## 3. Integration verification

- [x] 3.1 Verify equivalence (same sequence via stdin and via file → identical output and identical users file), blank-line handling, commands after `EXIT` not executed, and that `mvn test` and `mvn package` succeed; run the packaged JAR with a sample file and with a missing file
