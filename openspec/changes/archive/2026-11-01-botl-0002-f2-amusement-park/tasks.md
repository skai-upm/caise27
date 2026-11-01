# Tasks

Order follows the source guidance: baseline → date/pricing → groups → individual tickets → group tickets → attractions → persistence integration → `--fly` → UML → validation. Each group keeps `mvn test` green and F1 behaviour intact.

## 1. Baseline and controller

- [x] 1.1 Review F1 baseline (run `mvn test`, all existing tests pass) and set `<finalName>parque</finalName>`; verify `mvn package` produces `target/parque.jar`
- [x] 1.2 Introduce `StorageConfig` and the `Park` controller (system date initialised from the OS date, base price `40.00`) owning `UserManager`; route `CommandProcessor` user commands through `Park`; verify all F1 tests still pass after adapting construction

## 2. System date and price

- [x] 2.1 Implement `SYSTEM DATE;yyyy-MM-dd` (strict parsing, unchanged on error) and `SYSTEM PRICE;<Precio>` (positive decimal); verify with tests for valid/invalid dates, backward moves, and invalid prices

## 3. Groups

- [x] 3.1 Implement `Group`, `GroupRepository` (`groups.txt`, members by DNI) and `GROUP CREATE/ADD/REMOVE/DELETE` in `Park`/`CommandProcessor`; verify with tests for unknown/duplicate members, shared user references, and membership restored after restart

## 4. Individual tickets

- [x] 4.1 Implement abstract `Ticket` and `IndividualTicket`, `TicketRepository` (`tickets.txt`, holder snapshot, historical holders) and `TICKET CREATE` (exact `OK TICKET CREATED -> <Precio>`) / `TICKET DELETE` (strictly future); verify with tests for output text, past date, duplicates, non-retroactive price, refund boundaries, and restart
- [x] 4.2 Extend user deletion: block when the user holds a ticket dated ≥ system date; on success remove the user from groups and attractions and keep historical tickets; verify with tests for future/today/past tickets and history surviving restart after holder deletion

## 5. Group tickets

- [x] 5.1 Implement `GroupTicket` (composition of per-member `IndividualTicket`s with ids `<id>-<n>`, discount brackets, aggregation to `Group`) with persistence of `G`/`C` records; verify with tests for all brackets and the 2/3, 5/6, 10/11 boundaries
- [x] 5.2 Implement `GROUPTICKET CREATE` (exact `OK GROUP TICKET CREATED -> <Precio>`, all-or-nothing) and `GROUPTICKET DELETE` (strictly future, with constituents); verify with tests for empty group, id collisions, today/past rejection, and restart
- [x] 5.3 Enforce group locking (group ticket dated ≥ system date blocks `GROUP ADD/REMOVE/DELETE`) and constituent-based user-deletion blocking; verify with tests that the lock lifts when the date passes

## 6. Attractions

- [x] 6.1 Implement `Attraction` (atomic `enter(Ticket)`, `exit(Ticket)` via `Ticket.getUsers()`), `AttractionRepository` (`attractions.txt`, definitions only) and `ATTRACTION CREATE/ENTER/EXIT/STATUS`; verify with tests for date check, capacity, atomic group rejection, exit after date change, status content, and empty attractions after restart

## 7. Command integration and HELP

- [x] 7.1 Update `HELP` to list every F1 and F2 command with syntax; verify with a test asserting every syntax line from the spec is present

## 8. Fly mode

- [x] 8.1 Implement `[--fly] [commandFile]` parsing in `App`, per-run `tmp/<yyyyMMdd_HHmmss>/` storage, and recursive cleanup in `finally`; add `tmp/` and runtime data files to `.gitignore`; verify with tests that permanent files are untouched, outputs equal a normal run, and the directory is removed after EXIT, end of input, and unreadable file

## 9. UML

- [x] 9.1 Write `uml/model.puml` from the implemented code following the presentation rules in design.md; verify it renders with PlantUML without errors and that each relationship corresponds to an actual field

## 10. Verification

- [x] 10.1 Integration check: `mvn package`, then run `target/parque.jar` with an F2 command file (dates, groups, tickets, group tickets, attractions, deletion rules), restart to confirm persistence, and run the same file with `--fly` confirming permanent files are unchanged and `tmp/<run>` is removed
