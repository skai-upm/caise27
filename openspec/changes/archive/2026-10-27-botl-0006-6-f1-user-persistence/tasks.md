# Tasks

## 1. File store

- [x] 1.1 Create `persistence/UserFileStore` with `load()` (missing file → empty list, malformed lines skipped) and `save(users)` writing one `;`-separated line per user; verify with `UserFileStoreTest` (round trip, exact line text, missing file, corrupt line)

## 2. Integration

- [x] 2.1 Make `UserRepository` load from the store on creation and save after `add`; surface save failures as `ERROR: Could not save users`; verify with `UserRepositoryTest`
- [x] 2.2 Wire `Main` to `users.txt` (overridable in tests); verify `MainTest` restart scenario (create in one run, duplicate rejected in the next) and a manual `java -jar` run producing `users.txt`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F1-C04] Add user persistence`; verify with `git log -1 --format=%s`
