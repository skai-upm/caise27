# Tasks

## 1. F2-C01 — Park controller, system date and price

- [x] 1.1 Add `Park` singleton (private constructor, `getInstance()`, `start(ParkStorage, LocalDate)`, collections, `currentDate`, `basePrice` = 40.00, `commit(Runnable undo)`) and `ParkStorage` over a data directory; move `UserService`'s user map into `Park`; verify existing user tests (migrated to the new wiring) still pass and a test asserts `getInstance()` returns the same object and no public constructor exists
- [x] 1.2 Replace the `CommandProcessor` switch with a command registry (parameter count, syntax, handler) and derive `HELP` from it; verify all Delivery 1 command tests pass unchanged in behavior
- [x] 1.3 Add `SystemService` and `SYSTEM DATE;<yyyy-MM-dd>` / `SYSTEM PRICE;<Price>` (strict date parsing; positive price with ≤ 2 decimals, no regex); verify tests for valid/invalid dates and prices and that the price resets to 40.00 on a new `start`

## 2. F2-C02 — Groups

- [x] 2.1 Add `Group` (id, name, ordered member list without duplicates) and `GroupService` create/add/remove with validation (unique id, existing user, no duplicate member, non-blank fields); verify `GroupServiceTest` success and rejection cases

## 3. F2-C03 — Individual tickets

- [x] 3.1 Add abstract `Ticket` (id, date, `getPrice`, `getHolders`, `isActiveOn`, `isValidOn`) and `IndividualTicket` (user, base price, discount, price computation); verify `TicketTest` price computation and active/valid date logic
- [x] 3.2 Add `TicketService` `TICKET CREATE` (shared id space, existing user, date ≥ current, current base price, displayed price) and `TICKET DELETE` (standalone only, date strictly after current); verify tests for past/today/future dates, unknown user, duplicate id, price change affecting only new tickets
- [x] 3.3 Extend `USER DELETE` with the active-ticket check (derived), keeping historical tickets; verify tests: ticket today blocks, future ticket blocks until deleted, only historical tickets allow deletion and the ticket keeps its holder

## 4. F2-C04 — Group tickets, group deletion and locks

- [x] 4.1 Add `GroupTicket` (nullable group, unmodifiable composition, total = sum of composed prices, `discountFor`) and `GROUPTICKET CREATE` with derived `<id>:<dni>` identifiers checked atomically, non-empty group, date ≥ current, displayed total; verify totals 80/108/192/220 for 2/3/6/11 members at 40.00 and that a derived-id collision creates nothing
- [x] 4.2 Add derived group lock, enforce it in `GROUP ADD`/`GROUP REMOVE`, add `GROUP DELETE` (blocked by active group tickets, clears the group reference of historical tickets) and `GROUPTICKET DELETE` (strictly future, removes composition); verify lock/unlock via ticket deletion and via date change, and deletion rules
- [x] 4.3 Extend `USER DELETE` with composed-ticket and locked-group checks and removal from unlocked groups; verify tests for each case

## 5. F2-C05 — Attractions

- [x] 5.1 Add `Attraction` (id, name, positive capacity, transient occupant list) and `AttractionService` `ATTRACTION CREATE` and `ATTRACTION LIST` (emails or `NO USERS INSIDE`, read-only); verify creation/duplicate/invalid capacity tests and list output for empty and occupied attractions

## 6. F2-C06 — Individual attraction access

- [x] 6.1 Add `ATTRACTION ENTER` using `Ticket.getHolders()` with date-equals-current and capacity checks; verify tests for valid entry, wrong date, full attraction, unknown attraction/ticket, user already inside, presence kept after date change

## 7. F2-C07 — Group access, exit and status

- [x] 7.1 Make entry all-or-nothing for multi-holder tickets, add `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify tests: group of 4 vs 3 free places admits none, group entry succeeds with room, one member exits releasing one place, status lines contain id, name, capacity, inside count, available count and emails

## 8. F2-C08 — Persistence and fly mode

- [x] 8.1 Add `GroupRepository`, `TicketRepository` (I/G/C records with holder snapshots) and `AttractionRepository`; wire load order and save-all in `ParkStorage`; verify a restart test reconstructs groups, tickets (prices, composition, group reference), attractions with empty occupancy, and historical tickets of deleted users/groups
- [x] 8.2 Update `App` for `park.data.dir`, `--fly` argument forms, timestamped `tmp/<yyyyMMdd_HHmmss>/` directory and cleanup on `EXIT`/end of input; verify `AppTest` for `--fly` interactive and file runs (temporary directory removed, normal data untouched) and unchanged non-fly behavior

## 9. F2-C09 — PlantUML model

- [x] 9.1 Write `uml/model.puml` matching the implemented classes, attributes, inheritance, composition, directions and multiplicities; verify it with `java -jar plantuml-1.2024.7.jar -syntax uml/model.puml` and a test that every class declared in the diagram exists in `src/main/java`

## 10. F2-C10 — Delivery 2 integration

- [x] 10.1 Verify `HELP` lists every Phase 1 and Phase 2 command syntax in the §15 order with a test
- [x] 10.2 Run `mvn clean package`, then run the JAR with a mixed Phase 1/Phase 2 command file normally, with `--fly <file>` and with `--fly` interactively; verify outputs, invariants, persisted files after the normal run and absence of temporary data after fly runs
- [x] 10.3 Verify `pom.xml` still has no non-test dependencies and `src/main/java` uses no regex, serialization (`Serializable`, `ObjectOutputStream`) or encryption APIs
