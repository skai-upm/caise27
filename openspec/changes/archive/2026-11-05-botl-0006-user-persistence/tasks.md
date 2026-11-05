# Tasks

## 1. File storage

- [x] 1.1 Create `persistence.UserStorage` interface and `persistence.UserFileStorage` that reads/writes `;`-separated lines (missing file → empty list, malformed lines skipped); verify unit tests with a temp directory for round trip, field mapping, missing file and malformed line

## 2. Repository integration

- [x] 2.1 Make `UserRepository` load from a `UserStorage` on construction and save after `add`, keeping a no-arg in-memory constructor; verify unit tests that a second repository on the same file sees the users and that a rejected creation leaves the file unchanged
- [x] 2.2 Wire `UserFileStorage(Path.of("users.txt"))` in `App`; verify `App`/`User` contain no file I/O code

## 3. Integration

- [x] 3.1 In a temp directory, run the JAR with two `USER CREATE` commands, check `users.txt` lines, run again with a duplicate DNI and verify it is rejected; run `mvn clean package` and verify all tests pass
