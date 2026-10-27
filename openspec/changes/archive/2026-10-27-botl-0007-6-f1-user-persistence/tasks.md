# Tasks

## 1. File storage

- [x] 1.1 Create `UserFileStorage` (load/save in the `;` one-line-per-user format, missing file → empty, malformed lines skipped) and verify with `UserFileStorageTest` using a temp directory: exact line format, round trip of two users, missing file and malformed line handling

## 2. Repository and application wiring

- [x] 2.1 Make `UserRepository` load from and save to a `UserFileStorage` (keeping the in-memory constructor) and verify with `UserRepositoryTest` that a user saved through one repository instance is found by a new instance on the same file, and that deletion is persisted
- [x] 2.2 Wire `App` to `data/users.txt` through a data-directory parameter and verify with `AppTest` that a user created in one run exists in the next run (duplicate rejected) using a temp directory
- [x] 2.3 Run `mvn package` and verify with the JAR from a temp working directory that a user created in one execution is rejected as duplicate in a second execution and that `data/users.txt` holds the expected line
