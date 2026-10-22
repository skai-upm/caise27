# Tasks

## 1. Park controller and system settings (F2-C01)

- [x] 1.1 Add `ParkException` (with `UserException` extending it), the `Park` singleton (`getInstance`, `open(ParkStore, LocalDate)`, date, price defaulting to 40.00), and an initial `ParkStore` that wraps `FileUserRepository` in a data directory; make `UserService.authenticate` public; verify `ParkTest` checks the single instance, the initial date and price, and that `open` resets state
- [x] 1.2 Rework `CommandProcessor` to a table-driven `Command` registry built on `Park` (HELP generated from the table), add `SYSTEM DATE` and `SYSTEM PRICE` with strict date and manual price validation, and adapt `App` (`run(args, in, out, err, workDir)`) and existing Delivery 1 tests; verify all Delivery 1 tests pass and new tests cover valid and invalid dates and prices

## 2. User groups (F2-C02)

- [x] 2.1 Add the `Group` model, `groups.txt` repository, and Park/commands for `GROUP CREATE`, `GROUP ADD`, and `GROUP REMOVE` (unique ids, existing users, no duplicate members, undo on save failure); verify with `GroupTest` and Park and command tests

## 3. Individual tickets (F2-C03)

- [x] 3.1 Add the abstract `Ticket`, `IndividualTicket`, the `tickets.txt` repository (`INDIVIDUAL` lines with holder snapshot), the shared id check, `TICKET CREATE` (current price, no past dates, price printed), and `TICKET DELETE` (strictly future only); verify with model, repository, and command tests
- [x] 3.2 Make `USER DELETE` reject users with an active ticket while historical tickets do not block, keeping historical tickets referencing the deleted user; verify with tests for the active, future-deleted-then-allowed, and historical cases

## 4. Group tickets (F2-C04)

- [x] 4.1 Add `GroupDiscount` and `GroupTicket` (composition of `IndividualTicket`s with `<id>:<dni>` ids, nullable group, summed price), `GROUP`/`MEMBER` persistence, and `GROUPTICKET CREATE` (non-empty group, no past date, all-or-nothing id checks, total printed); verify with discount-bracket tests (2, 3, 6, 11 members) and command tests including derived-id conflicts
- [x] 4.2 Add `GROUPTICKET DELETE` (strictly future, removes the composition), `GROUP DELETE`, the derived group lock for ADD/REMOVE/DELETE, `TICKET DELETE` rejection for group tickets and components, and `USER DELETE` locked-group and group-membership cleanup; verify with tests for locking, unlocking after deletion, historical tickets keeping null group references, and the source example sequence

## 5. Attractions (F2-C05)

- [x] 5.1 Add the `Attraction` model, `attractions.txt` repository, `ATTRACTION CREATE` (positive integer capacity), and `ATTRACTION LIST` (read-only, empty message); verify with `AttractionTest` and command tests

## 6. Individual attraction access (F2-C06)

- [x] 6.1 Add `ATTRACTION ENTER` through the `Ticket` abstraction (valid on today, active holders, not already inside any attraction, capacity), `ATTRACTION EXIT` by email, and occupant removal on `USER DELETE`; verify entry, invalid-date, unknown-ticket, exit, and presence-after-date-change tests

## 7. Group attraction access (F2-C07)

- [x] 7.1 Verify group-ticket entry is all-or-nothing within capacity, members can exit individually, and add `ATTRACTION STATUS` (id, name, capacity, occupants, free places, emails); verify with tests including the capacity-overflow case

## 8. Complete persistence and --fly (F2-C08)

- [x] 8.1 Make `ParkStore.saveAll` write all four files via temp-then-move and load users, groups, attractions, and tickets in dependency order, resolving former users from ticket snapshots and deleted groups as null, with attractions starting empty; verify a full round-trip test including historical tickets of deleted users and groups and malformed-line warnings
- [x] 8.2 Add `--fly` handling in `App` (timestamped `tmp/` directory, same `ParkStore`, recursive cleanup in `finally` for EXIT, end of input, and unreadable file); verify `AppTest` cases for interactive and file `--fly`, cleanup, and untouched normal data

## 9. UML model (F2-C09)

- [x] 9.1 Write `uml/model.puml` (classes, enum, inheritance, compositions, associations with directions and multiplicities) and a `UmlModelTest` that checks each diagram class and relationship field exists in the code; verify the test passes

## 10. Delivery 2 integration (F2-C10)

- [x] 10.1 Update `README.md` with the Delivery 2 commands, data files, and `--fly`; verify the documented syntax matches the HELP output
- [x] 10.2 Run `mvn clean package` (all tests green) and execute the source example `input_fase2.txt` with the JAR both normally and with `--fly` from a scratch directory; verify the expected confirmations and totals (TG01 total 108.00, T002 at 45.50), that Marta and G02 are deleted, that a restart reloads the state, and that `--fly` leaves no files behind
- [x] 10.3 Resolve the Sonar quality-gate failure from the first apply analysis (1 new violation: java:S2259, possible null from `containerOf()` in `Park.deleteTicket`) without changing behavior; verify all tests still pass and the SDDX apply analysis reports quality gate `OK`
