# Tasks

## 1. File store

- [x] 1.1 Create `persistence.UserFileStore(Path)` with `load()` (missing file → empty list; 7-field lines mapped in order; malformed lines skipped) and `save(List<User>)` (one `;`-joined line per user, UTF-8); verify with `UserFileStoreTest` using a temp directory: round trip, field mapping, missing file, malformed line

## 2. Repository integration

- [x] 2.1 Add `UserRepository(UserFileStore)` that loads on construction and saves after `add`; keep the in-memory constructor; verify tests: users loaded at construction, file rewritten after add, uniqueness checks see loaded users
- [x] 2.2 Wire `App` to `new UserRepository(new UserFileStore(Path.of("users.txt")))` and add `users.txt` to `.gitignore`; verify `App` and `User` contain no file read/write code (grep for `Files.`/`FileReader`/`FileWriter`)

## 3. Integration

- [x] 3.1 End-to-end in a temp directory: run the JAR creating a user, run it again with a duplicate DNI and a new user, and verify the duplicate is rejected, `users.txt` holds both users in order in the §7 format, and exit status is 0; run `mvn verify` for regression
