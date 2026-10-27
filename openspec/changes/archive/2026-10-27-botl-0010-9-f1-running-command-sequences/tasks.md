# Tasks

## 1. File error handling

- [x] 1.1 Distinguish nonexistent and unreadable command files in `App` with controlled error messages and verify with `AppTest` (missing file → "not found", directory → "cannot be read", no commands processed, no exception)

## 2. File-mode contract verification

- [x] 2.1 Verify with `AppTest` that the source file-mode example (with blank lines added) prints `USER CREATED`, `USER CREATED`, `USER UPDATED` in order, and that the same commands on standard input produce identical output from equal starting data
- [x] 2.2 Run `mvn package` and verify `java -jar target/amusement-park.jar input.txt` with the source example and with a nonexistent file behaves as specified
