# Tasks

## 1. F2-C01 — Park controller and system date

- [x] 1.1 Introduce `ParkException` (base of `UserException`) and the `Park` singleton with `start(ParkRepository, LocalDate)`, moving Delivery 1 user operations from `UserService` into it unchanged; port `UserServiceTest` to `ParkUserTest` and verify all Delivery 1 tests still pass
- [x] 1.2 Add system date and base price state (initial local date, 40.00) with `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` (strict date, manual positive-amount check) in `CommandProcessor`; verify `ParkSystemTest` covers singleton identity, valid/invalid date, valid/invalid price and reset on `start`
- [x] 1.3 Checkpoint F2-C01 (`[F2-C01] Add park controller and system date`): verify `mvn package` succeeds (milestone commit owned by the BOTL harness, see design.md)

## 2. F2-C02 — User groups

- [x] 2.1 Implement `Group` (id, name, member list) and `GROUP CREATE/ADD/REMOVE` in `Park` and `CommandProcessor` with uniqueness, existing-user, duplicate-member checks and the derived `isGroupLocked` hook; verify `ParkGroupTest` covers create, duplicate id, add once, unknown user/group, remove
- [x] 2.2 Checkpoint F2-C02 (`[F2-C02] Add user groups`): verify `mvn package` succeeds

## 3. F2-C03 — Individual tickets

- [x] 3.1 Implement abstract `Ticket` (id, date, active/valid checks, shared `applyDiscount`) and `IndividualTicket`, the shared identifier lookup, `TICKET CREATE` (existing user, non-past date, current price, displayed price) and `TICKET DELETE` (strictly future, standalone only); verify `ParkTicketTest` covers today/past/future, price change affecting only new tickets, unknown user, duplicate id, delete rules
- [x] 3.2 Extend `USER DELETE` to reject users with an active individual ticket and keep historical tickets referencing the user; verify tests for today's ticket blocking, future ticket deleted then user deletable, historical ticket not blocking
- [x] 3.3 Checkpoint F2-C03 (`[F2-C03] Add individual tickets`): verify `mvn package` succeeds

## 4. F2-C04 — Group tickets

- [x] 4.1 Implement `GroupTicket` (composition of `IndividualTicket`, nullable group, total = sum, discount brackets) and `GROUPTICKET CREATE` with all-or-nothing id checks including derived `<id>:<dni>`; verify `ParkGroupTicketTest` covers composition ids, totals 80/108/192/220, empty group, past date, derived-id collision creating nothing
- [x] 4.2 Implement `GROUPTICKET DELETE` (strictly future, deletes composition), `GROUP DELETE` (unlocked only, clears group reference on historical tickets) and enforce derived locks on GROUP ADD/REMOVE/DELETE and on USER DELETE (constituent tickets, locked groups, membership removal); verify tests for today's lock, unlock after future deletion, unlock when tickets become historical, TICKET DELETE rejecting constituents, user deletion cases
- [x] 4.3 Checkpoint F2-C04 (`[F2-C04] Add group ticket purchase`): verify `mvn package` succeeds

## 5. F2-C05 — Attractions

- [x] 5.1 Implement `Attraction` (id, name, positive capacity, transient occupant list) with `ATTRACTION CREATE` and read-only `ATTRACTION LIST`; verify `ParkAttractionTest` covers create, duplicate id, invalid capacity, empty list, unknown attraction
- [x] 5.2 Checkpoint F2-C05 (`[F2-C05] Add attractions`): verify `mvn package` succeeds

## 6. F2-C06 — Individual attraction access

- [x] 6.1 Implement `ATTRACTION ENTER` via `Ticket.getHolders()` and `Attraction.enter` (valid today, capacity, not already inside); verify tests for valid entry, wrong date, full attraction, unknown ticket/attraction, presence kept after date change, LIST showing the occupant
- [x] 6.2 Checkpoint F2-C06 (`[F2-C06] Add individual attraction access`): verify `mvn package` succeeds

## 7. F2-C07 — Group attraction access

- [x] 7.1 Verify indivisible group entry through the same operation and implement `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify tests for capacity 3 vs group of 4 (nobody enters), group of 4 entering, one member exiting releasing one place, exit of non-occupant, STATUS content
- [x] 7.2 Checkpoint F2-C07 (`[F2-C07] Add group attraction access`): verify `mvn package` succeeds

## 8. F2-C08 — Park persistence and --fly

- [x] 8.1 Extract the `TextFile` helper, add `GroupRepository`, `TicketRepository`, `AttractionRepository` and the aggregating `ParkRepository(Path dir)`, load/save everything from `Park` with per-operation undo on save failure; verify `ParkPersistenceTest` round-trips groups, tickets (composition, price, group, deleted-user and deleted-group history) and attractions with empty occupants, and that `users.txt` format is unchanged
- [x] 8.2 Add `--fly [file]` handling in `App` (timestamped `tmp/<yyyyMMdd_HHmmss>/`, same repositories, recursive cleanup in `finally`); verify `AppTest` covers fly with file, fly interactive, cleanup after EXIT and end of input, and normal data untouched
- [x] 8.3 Checkpoint F2-C08 (`[F2-C08] Extend park persistence`): verify `mvn package` and `java -jar target/nombre-aplicacion.jar --fly <file>` leave no `tmp/<timestamp>` directory

## 9. F2-C09 — PlantUML model

- [x] 9.1 Write `uml/model.puml` (classes, enum, ticket hierarchy, composition, associations with directions and multiplicities, relevant attributes) matching the code; verify `java -jar plantuml.jar -syntax uml/model.puml` reports no error and review it class-by-class against `src/main/java`
- [x] 9.2 Checkpoint F2-C09 (`[F2-C09] Add PlantUML class model`): verify `mvn package` succeeds

## 10. F2-C10 — Delivery 2 integration

- [x] 10.1 Extend HELP with the exact §15 syntax for all Delivery 1 and 2 commands; verify `CommandProcessorTest` asserts every syntax line
- [x] 10.2 Add an end-to-end `input_fase2`-style test mixing Phase 1/2 commands (valid and invalid) in file, stdin and `--fly` modes; verify outputs, invariants and persisted state
- [x] 10.3 Checkpoint F2-C10 (`[F2-C10] Complete delivery 2 integration`): verify a fresh `mvn clean package` passes all tests and the JAR runs interactively, with a file and with `--fly` a file
