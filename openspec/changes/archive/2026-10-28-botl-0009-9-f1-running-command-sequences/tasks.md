# Tasks

## 1. File mode hardening

- [x] 1.1 Report nonexistent and unreadable paths (e.g. directory) with `Error: cannot read file '<path>'` and exit status 1 before reading; verify unit tests for a missing file and a directory
- [x] 1.2 Add an end-to-end test running the same command sequence (with blank lines) from standard input and from a file on separate fresh data files; verify identical output and identical stored users
- [x] 1.3 Run `mvn package` and the JAR with a sample `input.txt`, with stdin piping the same lines, and with a directory argument; verify identical outputs and exit status 1 for the directory

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C07] Add command file input`; verify with `git log -1 --format=%s`
