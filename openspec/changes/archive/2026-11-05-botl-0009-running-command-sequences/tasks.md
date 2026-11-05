# Tasks

## 1. File mode

- [x] 1.1 Make `App` open `args[0]` as a UTF-8 reader and run the same `InteractiveSession`/`CommandProcessor` on it; verify a unit test running `App` logic on a temp file prints responses in file order and skips blank lines
- [x] 1.2 Handle nonexistent/unreadable files with an `ERROR` message naming the file and exit status 1; verify by running the JAR with a missing file and with a directory

## 2. Integration

- [x] 2.1 Run the same command sequence via standard input and via a file in two temp directories and verify identical output and `users.txt`; run `mvn clean package` and verify all tests pass
