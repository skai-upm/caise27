# Tasks

## 1. Group access

- [x] 1.1 Widen ticket resolution for `ATTRACTION ENTER` to any top-level ticket and verify with unit tests: whole group enters, insufficient capacity lets nobody in, a member already inside blocks the whole group, wrong-date group ticket rejected

## 2. Exit and status

- [x] 2.1 Add `AttractionService.exit` and `status` and verify with unit tests: exit frees a place, group members exit individually, non-occupant rejected, status shows all fields for mixed and empty attractions
- [x] 2.2 Dispatch `ATTRACTION EXIT`/`ATTRACTION STATUS`, list them in `HELP`, and verify with `CommandProcessor` tests

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR with a group-ticket enter/status/exit sequence and verify outputs
- [x] 3.2 Run `openspec validate botl-0018-access-using-group-tickets --strict` and verify the change is valid
