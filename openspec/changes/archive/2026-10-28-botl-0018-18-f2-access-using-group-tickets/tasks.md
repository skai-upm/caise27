# Tasks

## 1. Group entry, exit and status

- [x] 1.1 Remove the group-ticket guard from `ParkController.enterAttraction`; verify tests: a group enters together, a group exceeding free places enters nobody
- [x] 1.2 Add `AttractionService.exit` and the `ATTRACTION EXIT` command; verify tests for individual exit of group members, freed capacity and user-not-inside error
- [x] 1.3 Add `AttractionService.status` and the `ATTRACTION STATUS` command (all required fields) and list both commands in `HELP`; verify tests after mixed entries and for an empty and an unknown attraction
- [x] 1.4 Run `mvn package` and a JAR session with group entry, a failing group entry, exits and status; verify responses

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F2-C07] Add group attraction access`; verify with `git log -1 --format=%s`
