# Tasks

## 1. Baseline and controller

- [x] 1.1 Review the F1 baseline: run `mvn test` and confirm all existing tests pass before changes
- [x] 1.2 Add `StoragePaths`, `ParkController` (system date initialized from `LocalDate.now()`, base price 40.00, `SYSTEM DATE` / `SYSTEM PRICE` operations with validation) and switch `CommandProcessor` to delegate to it; move user deletion behind `UserManager.authenticate` + `delete(User)`; verify F1 tests (adapted to the new wiring) and new tests for valid/invalid/backward dates and valid/invalid prices pass
- [x] 1.3 Set `<finalName>parque</finalName>` and verify `mvn package` produces `target/parque.jar`

## 2. Groups

- [x] 2.1 Implement `Group`, `GroupStorage` (`id;name;dni,...`) and `GroupManager` (create, add, remove, delete, find, removeUserEverywhere) with rollback on save failure; verify unit tests for create/duplicate id/empty name, add unknown/duplicate user, remove non-member, delete, and restart restoring references to the same registered `User` objects
- [x] 2.2 Wire `GROUP CREATE/ADD/REMOVE/DELETE` through `ParkController` and `CommandProcessor`; verify command-level tests and that an updated user is reflected in the group

## 3. Individual tickets

- [x] 3.1 Implement `Ticket` (abstract: id, date, price, `getUsers`, `isActiveOn`, `isValidOn`) and `IndividualTicket`; verify unit tests for the date predicates
- [x] 3.2 Implement `TicketStorage` (`I`/`G`/`C` lines with holder snapshot, detached user/group reconstruction) and `TicketManager` (createIndividual, deleteIndividual, find, hasActiveTicket, save); verify round-trip tests including a ticket whose user no longer exists
- [x] 3.3 Wire `TICKET CREATE` (reply `OK TICKET CREATED -> <Precio>`) and `TICKET DELETE`; verify tests for success, unknown user, duplicate id, past/malformed date, price change not affecting bought tickets, and before/equal/after refund rule
- [x] 3.4 Extend user deletion: reject with current/future tickets or locked group, remove deleted user from groups and attractions, re-save tickets; verify the SYSTEM DATE 2026-10-01 → 2026-10-16 scenario, today-blocks case, and historical ticket retained after restart

## 4. Group tickets

- [x] 4.1 Implement `GroupTicket` (group aggregation, constituent composition, `getUsers`), discount brackets and `TicketManager.createGroup/deleteGroup/isGroupLocked` with all-or-nothing behavior and `<id>-<n>` constituent ids; verify tests for prices at 2/3/5/6/10/11 members, id collisions creating nothing, and composition preserved after later membership changes
- [x] 4.2 Wire `GROUPTICKET CREATE` (reply `OK GROUP TICKET CREATED -> <Precio>`) and `GROUPTICKET DELETE`, and enforce group locks on `GROUP ADD/REMOVE/DELETE`; verify tests for locked/unlocked transitions with date changes, member deletion blocked, current group ticket not deletable, future one deleted with all constituents, and restart restoring group tickets

## 5. Attractions

- [x] 5.1 Implement `Attraction` (enter with date/duplicate/capacity checks before mutation, exit, status), `AttractionStorage` (`id;name;capacity`) and `AttractionManager`; verify unit tests for individual entry, atomic group rejection at 3 free places / 4 users, wrong-date rejection, exit without date check, and empty occupancy after restart
- [x] 5.2 Wire `ATTRACTION CREATE/ENTER/EXIT/STATUS`; verify the status scenario (T001 + TG01 → 4 present/16 free, exit T001 → 3/17) and invalid create parameters

## 6. Execution modes, help and --fly

- [x] 6.1 Update `HELP` to list all F2 commands; verify a test asserting every command syntax appears
- [x] 6.2 Implement `--fly` in `App`: argument parsing (flag anywhere, first other arg = file), timestamped `tmp/` directory, shared storages via `StoragePaths`, recursive cleanup in `finally` plus shutdown hook; add `tmp/` to `.gitignore`; verify tests that a fly run leaves normal files untouched and removes its directory, for interactive, file and missing-file cases, and that normal and fly replies are identical

## 7. UML and verification

- [x] 7.1 Write `uml/model.puml` from the final code following D12 presentation rules; verify it renders with PlantUML without errors and that every relationship corresponds to a field in the code
- [x] 7.2 Integration check: `mvn package`, run `java -jar target/parque.jar input_fase2.txt` and `--fly` with a scenario covering dates, groups, tickets, discounts, attractions and user deletion; verify outputs, that permanent files are unchanged by the fly run, restart restores state, and `mvn test` passes
