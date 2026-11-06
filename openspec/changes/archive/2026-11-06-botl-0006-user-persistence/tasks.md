# Tasks

## 1. Persistence component

- [x] 1.1 Create `es.upm.park.persistence.PersistenceException` and `UserFileStore` (load/save `;`-separated lines with java.nio, UTF-8; missing file → empty; malformed/blank lines skipped) and verify with unit tests for round-trip, exact line format, field mapping of the `Pedro` example, missing file and malformed line

## 2. Repository and wiring

- [x] 2.1 Make `UserRepository` optionally backed by a `UserFileStore` (load in constructor, save-before-update on add) and verify with unit tests that users are reloaded by a new repository and that a failed save leaves memory unchanged
- [x] 2.2 Report `PersistenceException` from `CommandProcessor` as an `Error:` message and verify with a unit test
- [x] 2.3 Wire `Main` with `UserFileStore` at `park.users.file` (default `users.txt`), add `users.txt` to `.gitignore`, and verify with a `MainTest` that users created in one run are rejected as duplicates in a second run using the same file

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; then run the JAR twice in a temp directory (create Ana, then try to create Ana again) and verify the second run reports a duplicate DNI and `users.txt` holds one line
- [x] 3.2 Run `openspec validate botl-0006-user-persistence --strict` and verify the change is valid
