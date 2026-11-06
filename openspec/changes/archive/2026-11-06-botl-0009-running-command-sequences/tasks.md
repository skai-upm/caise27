# Tasks

## 1. Input selection

- [x] 1.1 Make `Main` read from the file given as first argument (UTF-8) or from standard input otherwise, both through the single `run` loop, and verify with `MainTest` that a file's commands run in file order with blank lines ignored and standard input is not read in file mode
- [x] 1.2 Handle nonexistent or unreadable command files by printing `Error: cannot read command file <path>` and returning normally, and verify with `MainTest` for a missing file and a directory path
- [x] 1.3 Verify with `MainTest` that the same command lines give identical output and stored users in stdin and file mode

## 2. Integration

- [x] 2.1 Run `mvn package` and verify all tests pass; run the JAR in a temp dir with a command file (create, blank line, update) and with `missing.txt`, and verify ordered results and a controlled error without stack trace
- [x] 2.2 Run `openspec validate botl-0009-running-command-sequences --strict` and verify the change is valid
