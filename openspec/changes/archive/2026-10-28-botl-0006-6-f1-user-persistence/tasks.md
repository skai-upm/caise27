# Tasks

## 1. Persistence layer

- [x] 1.1 Create `persistence.UserStorage` and `persistence.UserFileStorage` (one `;`-separated line per user, load skipping corrupt lines with warnings, missing file = empty); verify unit tests for save format, round trip, missing file and corrupt lines

## 2. Integration

- [x] 2.1 Make `UserService` save the new user list through `UserStorage` before updating the repository, failing the operation if saving fails; verify unit tests with a fake storage (saved contents, failed save leaves repository unchanged)
- [x] 2.2 Load stored users at startup in `App` (data file `users.txt`, configurable for tests) and print load warnings to stderr; verify a test creating a user in one run and getting a duplicate-DNI error in a second run over the same file
- [x] 2.3 Run `mvn package` and run the JAR twice from a temporary directory; verify `BUILD SUCCESS`, the `users.txt` contents, and the duplicate rejection in the second run

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F1-C04] Add user persistence`; verify with `git log -1 --format=%s`
