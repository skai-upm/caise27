# Tasks

## 1. Commands

- [x] 1.1 Add `HELP` to `CommandProcessor` (five syntaxes plus modifiable fields, 0 fields) and verify with unit tests that the text contains each syntax and stored users are unchanged
- [x] 1.2 Add `EXIT` (closing message, sets `isExitRequested`, 0 fields; `EXIT;now` rejected) and verify with unit tests

## 2. Shared loop

- [x] 2.1 Stop `Main.run` after a processed `EXIT` and verify with `MainTest` that, in file mode, a `USER CREATE` after `EXIT` is not executed and, in stdin mode, `HELP` after `EXIT` is not printed; also verify `HELP` output is identical in both modes

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with a file `USER CREATE…/EXIT/USER CREATE…` and verify only the first user exists
- [x] 3.2 Run `openspec validate botl-0010-execution-control --strict` and verify the change is valid
