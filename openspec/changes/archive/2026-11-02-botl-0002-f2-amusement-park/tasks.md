# Tasks

## 1. F2-C01 — Park controller and system date

- [x] 1.1 Add `park.Park` singleton (users via `UserService`, date initialised to start date, price 40.00) and route existing USER commands through it; verify Delivery 1 tests still pass and a test asserts a single instance with no public constructor
- [x] 1.2 Add `SYSTEM DATE` and `SYSTEM PRICE` (strict date parsing, regex-free price check, ≤2 decimals, positive); verify with tests for valid/invalid dates and prices and that price changes do not alter existing tickets

## 2. F2-C02 — User groups

- [x] 2.1 Add `group.Group` and `GROUP CREATE` / `GROUP ADD` / `GROUP REMOVE`; verify with tests for duplicate id, unknown user/group, duplicate member and removal of non-members

## 3. F2-C03 — Individual tickets

- [x] 3.1 Add abstract `ticket.Ticket`, `IndividualTicket`, shared identifier lookup, `TICKET CREATE` (price shown) and `TICKET DELETE` (strictly future, standalone only); verify with tests for past/today/future dates, unknown user, duplicate id and price display
- [x] 3.2 Extend `USER DELETE` to reject users with active individual tickets and keep historical tickets; verify with tests for blocked, unblocked-after-date-change and history retained

## 4. F2-C04 — Group tickets

- [x] 4.1 Add `GroupTicket` (composition `<id>:<dni>`, group reference, discount brackets, total = sum) with `GROUPTICKET CREATE` / `GROUPTICKET DELETE`; verify with tests for 2/3/6/11 members (80/108/192/220), empty group, derived-id conflict creating nothing, and deletion rules
- [x] 4.2 Add group lock (derived, never stored) to `GROUP ADD` / `GROUP REMOVE`, add `GROUP DELETE` (null group reference on historical tickets), and extend `USER DELETE` for constituent tickets and locked groups with removal from groups; verify with tests for each rule

## 5. F2-C05 — Attractions

- [x] 5.1 Add `attraction.Attraction`, `ATTRACTION CREATE` and read-only `ATTRACTION LIST`; verify with tests for valid/invalid capacity, duplicate id, empty and non-empty listing without state change

## 6. F2-C06 — Individual attraction access

- [x] 6.1 Add `ATTRACTION ENTER` using `Ticket.getHolders()`; verify with tests for valid entry, wrong date, full attraction, unknown ids, duplicate presence and presence surviving a date change

## 7. F2-C07 — Group attraction access

- [x] 7.1 Verify all-or-nothing group entry through the same mechanism and add `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify with tests for insufficient capacity, individual exit of a group member and status contents

## 8. F2-C08 — Park persistence and fly mode

- [x] 8.1 Add `GroupFileRepository`, `TicketFileRepository`, `AttractionFileRepository` and `ParkStorage` (directory configuration, all-or-nothing save with restore) and wire every Park mutation to persist with in-memory undo on failure; verify with restart tests reconstructing groups, tickets, compositions, null group references, detached historical users and empty attractions
- [x] 8.2 Add `--fly` (timestamped `tmp/` directory, same repositories, recursive cleanup on EXIT and end of input); verify with tests for file and interactive fly runs leaving no directory and not touching normal data

## 9. F2-C09 — PlantUML model

- [x] 9.1 Write `uml/model.puml` matching the implemented classes, fields, inheritance, composition, directions and multiplicities; verify with `java -jar plantuml.jar -syntax uml/model.puml` and a manual code-to-diagram review

## 10. F2-C10 — Delivery 2 integration

- [x] 10.1 Extend HELP with all Delivery 1 and 2 commands; verify with a test asserting every syntax line
- [x] 10.2 Run the full suite and `mvn package`, then run the JAR with a mixed Phase 1/2 `input_fase2.txt` in file mode and `--fly` file mode plus an interactive `--fly` session; verify outputs match, normal data is unchanged by fly runs, and no `tmp/<timestamp>` remains
