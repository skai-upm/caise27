# Tasks

## 1. Group model

- [x] 1.1 Create `model/Group` and `repository/GroupRepository`, and expose groups from `ParkController` (reset on `start`); verify with `GroupTest` (no duplicate members, unmodifiable members, remove)

## 2. GROUP commands

- [x] 2.1 Implement GROUP CREATE, GROUP ADD, GROUP REMOVE and GROUP DELETE in `CommandProcessor`, list them in HELP; verify every user-groups scenario in `CommandProcessorTest`
- [x] 2.2 Remove a deleted user from all groups on USER DELETE; verify in `CommandProcessorTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C02] Add user groups`; verify with `git log -1 --format=%s`
