# Tasks

## 1. Repositories

- [x] 1.1 Create `TextFileRepository` (read/write all lines of one file, missing file = empty) and the four concrete repositories with the documented line formats; verify with unit tests in a temporary directory that each repository round-trips its elements and that two repositories with different directories touch only their own files
- [x] 1.2 Add package-private restore methods to `ParkController` (users, groups with members, attractions without occupants, individual tickets, group tickets with composition and optional group) and `ParkStorage.load/save`; verify with a unit test that saves a full state (including a historical group ticket of a deleted group), resets, loads and compares all elements, and that attractions come back empty

## 2. Application lifecycle

- [x] 2.1 Refactor `App` into `run(args, baseDir, in, out)` handling `[--fly] [file]`, normal data dir `data/`, fly dir `tmp/<yyyyMMdd_HHmmss>` (suffix on collision), load at start, save after each command, and recursive deletion of the fly dir in `finally`; update `AppTest` to use temporary base directories and verify all four execution forms, fly cleanup after EXIT and after end of file, fly isolation from permanent data, and restart reconstruction in normal mode
- [x] 2.2 Add `/data/` and `/tmp/` to `.gitignore`; verify `git status` shows no data files after running the JAR
- [x] 2.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR twice in normal mode in a scratch directory (create attraction, restart, `ATTRACTION STATUS`) and once with `--fly input.txt`, verifying reconstruction and that the `tmp/<timestamp>` directory is gone
