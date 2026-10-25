# Tasks

## 1. Central management and system settings (F2-C01)

- [x] 1.1 Add `persistence.ParkStorage` (data directory → user/group/ticket/attraction repositories) and switch `App` to the `park.data.dir` property; verify Delivery 1 tests still pass after adapting them
- [x] 1.2 Add singleton `controller.ParkController` (configure/reset, date, price, user operations delegated to `UserService`) and route `CommandProcessor` through it; add `SYSTEM DATE` and `SYSTEM PRICE`; verify `ParkControllerSettingsTest` (singleton identity, defaults, valid/invalid date and price)

## 2. Groups (F2-C02)

- [x] 2.1 Add `model.Group` and `GROUP CREATE/ADD/REMOVE` with duplicate-member and unknown-user checks; verify `GroupTest`/controller tests

## 3. Tickets (F2-C03, F2-C04)

- [x] 3.1 Add abstract `model.Ticket`, `IndividualTicket`, `TICKET CREATE/DELETE` (shared identifier check, past-date rejection, strictly-future deletion, current price) and ticket-aware `USER DELETE`; verify ticket and user-deletion tests
- [x] 3.2 Add `GroupDiscount`, `GroupTicket` (composition, derived ids, discounted prices, total), `GROUPTICKET CREATE/DELETE`, `GROUP DELETE`, derived group lock for `GROUP ADD/REMOVE/DELETE` and locked-group check in `USER DELETE`; verify discount brackets (2/3/6/11 members), lock/unlock and historical retention tests

## 4. Attractions (F2-C05 – F2-C07)

- [x] 4.1 Add `model.Attraction`, `ATTRACTION CREATE` and `ATTRACTION LIST`; verify creation validation and empty/non-empty listing tests
- [x] 4.2 Add `ATTRACTION ENTER` (individual and atomic group entry, date check, capacity), `ATTRACTION EXIT` and `ATTRACTION STATUS`; verify entry/exit/capacity/status tests

## 5. Persistence and --fly (F2-C08)

- [x] 5.1 Add `GroupFileRepository`, `TicketFileRepository`, `AttractionFileRepository`, load/save them from `ParkController` (detached users for deleted holders, cleared group refs, empty attractions on load); verify reload round-trip tests
- [x] 5.2 Add `--fly [file]` to `App` (timestamped `tmp/` directory, cleanup in all normal endings); verify `AppTest` checks isolation from permanent data and directory removal

## 6. Documentation (F2-C09)

- [x] 6.1 Extend `HELP` with every Delivery 2 command; verify `CommandProcessorTest` checks each syntax line
- [x] 6.2 Write `uml/model.puml` (classes, enum, inheritance, composition, directed associations, multiplicities) and update `README.md`; verify the diagram names match the source classes

## 7. Delivery 2 integration (F2-C10)

- [x] 7.1 Run `mvn package`, then the assignment's example sequence (`input_fase2.txt`) with and without `--fly`; verify expected outputs (group ticket total 108.00, `USER DELETED` for Marta after `TICKET DELETE`, `GROUP DELETED` for G02), that `--fly` leaves no data, and that a normal run persists and reloads
