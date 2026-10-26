# Tasks

## 1. Ticket hierarchy

- [x] 1.1 Make `Ticket` abstract and rename the concrete class to `IndividualTicket`, updating all usages and tests; verify `mvn test` passes unchanged
- [x] 1.2 Create `GroupTicket` (group, composed individual tickets, discount brackets, total); verify with unit tests for the 2/3, 5/6 and 10/11 boundaries (80, 108, 180, 192, 320, 220 at 40.00)

## 2. Persistence

- [x] 2.1 Extend `TicketRepository` with `data/grouptickets.txt` and the owning-group-ticket field (legacy lines still load); verify with `@TempDir` round-trip tests including a deleted group

## 3. Rules and commands

- [x] 3.1 Add `createGroupTicket`, `deleteGroupTicket`, `isGroupLocked`, internal-ticket protection in `TicketManager`; verify with unit tests for composition, empty group, id collisions, deletion boundaries and locking over time
- [x] 3.2 Add `GROUPTICKET CREATE/DELETE` to `CommandProcessor` and HELP and reject `GROUP ADD/REMOVE/DELETE` on locked groups; verify with command tests, including member deletion blocked by internal tickets
- [x] 3.3 Wire the group tickets file in `App`; verify with a jar session including a restart that keeps the group locked

## 4. Model and verification

- [x] 4.1 Update `uml/model.puml` with the ticket hierarchy, aggregation and composition; verify with `plantuml -checkonly`
- [x] 4.2 Run `mvn test` and verify all tests pass
