# Tasks

## 1. Entry with pass consumption

- [x] 1.1 Replace the entry hook with `passesToConsume`, add `EntryResult` and atomic `enter(T, passes, speed)`; override in `SpecialAttraction`; verify with model tests (SpeedPass selection, missing pass, full attraction keeps passes, Special Pass consumed once, SPEED rejected at SPECIAL)
- [x] 1.2 Add the `speed` entry operation to `ParkController`; verify with controller tests (individual, group atomicity, date/type failures keep passes, consumed state persisted)

## 2. Command

- [x] 2.1 Accept `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, reject other options, print consumed passes, add catalogue entry; verify with command tests and HELP tests

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with a SpeedPass/Special Pass entry scenario in `--fly` mode; verify outputs
- [x] 3.2 Commit with the exact message `[F3-C08] Extend attraction access and pass consumption`; verify with `git log -1 --format=%s`
