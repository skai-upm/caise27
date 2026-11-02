# Tasks

## 1. F2-C01 Park controller and system settings

- [x] 1.1 Introduce top-level `OperationException` replacing `user.UserOperationException`, and change `UserManager.update` to apply changes in place after a successful save (expose `authenticate` and `remove`); verify all existing Delivery 1 tests still pass, plus a test that the same `User` object is retained after an update
- [x] 1.2 Implement `park.Park` singleton with `DataPaths`, `load`, current date (local date at load) and base price (40.00), and move user commands in `CommandProcessor` to go through `Park`; verify `ParkTest` (same instance, defaults after load)
- [x] 1.3 Add `SYSTEM DATE;<yyyy-MM-dd>` (strict parsing) and `SYSTEM PRICE;<Price>` (positive, at most two decimals); verify `CommandProcessorTest` cases for valid and invalid date/price leaving state unchanged on rejection

## 2. F2-C02 User groups

- [x] 2.1 Implement `group.Group` and `Park` group operations `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE` (unique id, existing user, no duplicate member) with an in-memory lock check placeholder returning unlocked until tickets exist; verify `GroupTest`/`ParkGroupTest` for creation, duplicates, unknown group/user, add/remove

## 3. F2-C03 Individual tickets

- [x] 3.1 Implement abstract `ticket.Ticket` (identifier, date, price, users, activity, shared discount helper) and `IndividualTicket`; verify `TicketTest` for price and activity rules
- [x] 3.2 Add `TICKET CREATE` (shared-id check, existing user, non-past date, current base price, price displayed) and `TICKET DELETE` (strictly future, standalone only); verify `ParkTicketTest` incl. price change affecting only new tickets
- [x] 3.3 Make `USER DELETE` go through `Park` and reject users with active individual tickets (historical tickets allowed); verify tests for today/future/historical tickets

## 4. F2-C04 Group tickets

- [x] 4.1 Implement `GroupTicket` (nullable group, fixed composition, total price) with discount brackets; verify bracket totals 80/108/192/220 at base 40.00
- [x] 4.2 Add `GROUPTICKET CREATE` (non-empty group, non-past date, atomic check of main and derived `<id>:<dni>` identifiers) and `GROUPTICKET DELETE` (strictly future, removes composition); verify collision leaves nothing created and composition-ticket `TICKET DELETE` is rejected
- [x] 4.3 Derive group locks from active group tickets for `GROUP ADD`/`GROUP REMOVE`, add `GROUP DELETE` (unlocked only, nulls historical tickets' group), and extend `USER DELETE` to composition tickets, locked groups and removal from groups; verify lock/unlock and USER DELETE scenarios

## 5. F2-C05 Attractions

- [x] 5.1 Implement `attraction.Attraction`, `ATTRACTION CREATE` (unique id, positive integer capacity) and read-only `ATTRACTION LIST`; verify empty, occupied and unknown-attraction listings

## 6. F2-C06 Individual attraction access

- [x] 6.1 Add `ATTRACTION ENTER` using `Ticket.getUsers()` for individual tickets: ticket valid today, active registered user, capacity, no duplicate occupant; verify valid entry, other date, full attraction, unknown ticket/attraction, and presence unaffected by later `SYSTEM DATE`

## 7. F2-C07 Group attraction access, exit and status

- [x] 7.1 Verify and complete all-or-nothing group entry through the same `ATTRACTION ENTER` path; add `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify insufficient capacity (none enter), individual exit of a group member and status counts

## 8. F2-C08 Park persistence and --fly

- [x] 8.1 Implement `GroupStorage`, `TicketStorage` (I/G/C records with user snapshots) and `AttractionStorage`; wire `Park` to load them and save after each mutation with rollback on failure; verify restart reconstruction (members, ticket users, composition, group reference), empty occupancy after restart, historical tickets of deleted users/groups, and a write-failure test
- [x] 8.2 Implement `persistence.TemporaryDirectory` and `--fly` handling in `App` for all four argument forms with cleanup in `finally`; verify `AppTest` for `--fly` interactive and file modes (directory removed, permanent files untouched) and unchanged Delivery 1 argument behavior

## 9. F2-C09 PlantUML model

- [x] 9.1 Write `uml/model.puml` from the final code (ticket hierarchy, composition, directed associations, multiplicities, enum); verify it renders with the cached PlantUML 1.2024.7 jar (`-checkonly`) and review each relationship against the source

## 10. F2-C10 Delivery 2 integration

- [x] 10.1 Extend `HELP` with the exact Delivery 2 syntax list and update the Delivery 1 help test; verify every command appears
- [x] 10.2 Run `mvn clean package`, then run the JAR with a mixed Delivery 1/2 command file in normal mode, again after restart, and with `--fly` (file and interactive); verify outputs, persisted files and that no `tmp/` data remains
- [x] 10.3 Verify restrictions: no runtime dependencies, no `java.io.Serializable`/`ObjectOutputStream`, no regex APIs in `src/main/java`, relationships held as object references
