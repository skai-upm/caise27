# Tasks

## 1. Storage classes

- [x] 1.1 Create `persistence.DataLocation` and `GroupFileStorage`, `AttractionFileStorage`, `TicketFileStorage` (formats from design, parent directory created on save); verify unit round-trip tests incl. a detached group ticket and a ticket of a deleted user

## 2. Park state

- [x] 2.1 Add `ParkController.initialize(DataLocation)` reconstructing users, groups, attractions (empty) and tickets with their relationships, and `saveState()`; call it from `CommandProcessor` after successful state-changing commands; verify a restart test (groups, prices, lock, empty attractions, historical ticket of a deleted user)
- [x] 2.2 Switch `App` to a data directory and add `--fly [file]` with a timestamped directory under `tmp/` deleted at the end; update existing tests; verify tests for both fly forms (cleanup after EXIT and after end of input, permanent data untouched) and unchanged normal mode
- [x] 2.3 Add `tmp/`, `groups.txt`, `tickets.txt`, `attractions.txt` to `.gitignore`; run `mvn package` and the JAR in the four argument forms from a temporary directory; verify files, reconstruction and fly cleanup

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F2-C08] Extend park persistence`; verify with `git log -1 --format=%s`
