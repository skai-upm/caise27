# Tasks

## 1. User file format

- [x] 1.1 Write users in the assignment order and read the earlier formats in `TextUserRepository`; verify with repository tests (exact line, round trip, 2/3-field and previous 7-field lines, malformed line reported)
- [x] 1.2 Add an integration test creating users with `USER CREATE`, restarting and authenticating; verify it passes along with the architecture test

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR twice in a scratch directory (create, then list via a duplicate `USER CREATE` rejection); verify `data/users.txt` content
- [x] 2.2 Commit with the exact message `[F1-C04] Add user persistence`; verify with `git log -1 --format=%s`
