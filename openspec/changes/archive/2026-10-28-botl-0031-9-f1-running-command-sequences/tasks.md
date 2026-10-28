# Tasks

## 1. Verification

- [x] 1.1 Add `CommandFileModeTest` (same output/state for stdin and file with the source examples, blank lines ignored, file order, missing and unreadable paths handled); verify it passes

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with the source file example and interactively in a scratch directory; verify identical outputs
- [x] 2.2 Commit with the exact message `[F1-C07] Add command file input`; verify with `git log -1 --format=%s`
