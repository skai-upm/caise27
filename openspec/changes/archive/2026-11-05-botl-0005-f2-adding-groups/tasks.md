# Tasks

## 1. Domain

- [x] 1.1 Add `ParkException`, `Group` (encapsulated members set) and `UserDirectory`/`GroupTicketDates` ports; verify `Group` unit tests (no duplicate members, unmodifiable view)
- [x] 1.2 Add group operations and derived `isGroupLocked` to `ParkController` (create/add/remove/delete with all rejections); verify unit tests for each positive/negative scenario, including lock on today/future ticket, no lock on past tickets, and re-evaluation after date change

## 2. Commands

- [x] 2.1 Implement `GroupCommands` (`GROUP CREATE/ADD/REMOVE/DELETE`), register in `App.run`, list in `HELP`; verify command-level tests for messages and malformed input

## 3. Verification

- [x] 3.1 Run `mvn -q package`; verify all tests pass and the JAR processes `GROUP CREATE;G01;Familia Garcia`, `GROUP DELETE;G01`, `EXIT`
