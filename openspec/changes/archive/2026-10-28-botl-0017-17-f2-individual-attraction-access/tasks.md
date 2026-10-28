# Tasks

## 1. Access

- [x] 1.1 Implement `AttractionService.enter` on the common ticket abstraction (date = current date, no duplicates, all-or-nothing capacity); verify unit tests for valid entry, wrong date, full attraction, duplicate, and an all-or-nothing case with a multi-user ticket
- [x] 1.2 Add `ParkController.enterAttraction` (ticket and attraction lookup, group tickets rejected in this milestone) and the `ATTRACTION ENTER` command with `HELP` entry; verify processor tests including presence after a date change
- [x] 1.3 Run `mvn package` and a JAR session entering with tickets for today and tomorrow and a full attraction; verify responses

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F2-C06] Add individual attraction access`; verify with `git log -1 --format=%s`
