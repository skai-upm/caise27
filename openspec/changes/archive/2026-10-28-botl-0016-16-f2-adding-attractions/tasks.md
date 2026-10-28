# Tasks

## 1. Attractions

- [x] 1.1 Create `model.Attraction` and `repository.AttractionRepository`; verify unit tests for data, occupants order and copy-on-read
- [x] 1.2 Create `service.AttractionService` (create with unique id/non-empty name/capacity ≥ 1, list occupant emails) and expose it from `ParkController`; verify unit tests
- [x] 1.3 Handle `ATTRACTION CREATE` (capacity parsing) and `ATTRACTION LIST` in `CommandProcessor` and list them in `HELP`; verify processor tests for success, invalid capacities, duplicates, unknown attraction and empty/occupied listing
- [x] 1.4 Run `mvn package` and a JAR session with the statement's examples; verify responses

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F2-C05] Add attractions`; verify with `git log -1 --format=%s`
