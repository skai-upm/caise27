# Tasks

## 1. Controlled handling

- [x] 1.1 Add a `RuntimeException` catch-all in `CommandProcessor.process` returning an `ERROR` message; verify a test with a failing storage where creation prints `ERROR` and `HELP` still works, plus the malformed-input examples
- [x] 1.2 Guard start-up in `App.run` so an unreadable users file prints `ERROR` naming it and returns 1; verify a test with `users.txt` as a directory

## 2. Restrictions

- [x] 2.1 Rewrite email validation without regular expressions (same behavior); verify existing `UserValidatorTest` still passes
- [x] 2.2 Replace `split("\\R")` in tests with `lines()`, add a compliance test scanning production sources for regex/encryption usage and asserting all `pom.xml` dependencies are test-scoped; verify it passes

## 3. Integration

- [x] 3.1 Add an end-to-end test running the assignment's basic example in interactive and file mode, verify identical outputs and final `users.txt`; run `mvn clean package` from clean and the JAR in both modes and verify success
