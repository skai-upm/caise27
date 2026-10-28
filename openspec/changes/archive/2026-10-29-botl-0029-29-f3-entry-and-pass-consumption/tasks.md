# Tasks

## 1. Entry with passes

- [x] 1.1 Add `EntryResult` and extend `Attraction` entry with the SpeedPass option and pass selection/consumption after all checks; move Special Pass handling into `SpecialAttraction.passesToConsume`; verify model tests: individual SPEED, group SPEED atomicity (missing pass, capacity), special consumption, SPEED rejected at special, nothing consumed on failure
- [x] 1.2 Propagate the option through `AttractionService`, `ParkController` and `CommandProcessor` (`;SPEED` variant, output of consumed passes, catalog entry); verify processor tests
- [x] 1.3 Run `mvn package` and a JAR session with SPEED entries and a special attraction; verify responses

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F3-C08] Extend attraction access and pass consumption`; verify with `git log -1 --format=%s`
