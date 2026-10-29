# Tasks

## 1. Model

- [x] 1.1 Add `dni` to `User` (constructor `User(email, dni)`, getter) and update existing tests to pass DNIs; verify `mvn test` still passes
- [x] 1.2 Create `GroupTicket` (unmodifiable composition, releasable group reference, price = sum of composition, `identifiers()` including composition, `involves`) with static discount bracket and discounted-price helpers; verify with unit tests for brackets 2/3/5/6/10/11 at 40.00 (40, 36, 36, 32, 32, 20), total 192.00 for 6 members, identifiers and involvement

## 2. Central management operations

- [x] 2.1 Add `createGroupTicket` (existing non-empty group, date not past, all identifiers free, no partial creation) and real `hasActiveGroupTicket`; verify with unit tests for composition ids `TG01:<dni>`, empty group, past date, conflict with an existing `TG01:12345678Z` creating nothing, and composition unchanged after the group changes
- [x] 2.2 Add `deleteGroupTicket` (strictly future, removes composition), make `deleteTicket` reject composed tickets and group tickets, release group references on GROUP DELETE; verify with unit tests for future/today deletion, composed-ticket rejection, lock on GROUP ADD/REMOVE/DELETE while active, unlock after deleting the future ticket or when the date passes, and null group reference after deleting a group with historical tickets
- [x] 2.3 Add `canDeleteUser(User)`; verify with unit tests for an active composed ticket and for a member of a locked group without a ticket

## 3. Commands

- [x] 3.1 Dispatch `GROUPTICKET CREATE` (prints total) and `GROUPTICKET DELETE`, add both to HELP; verify with processor tests for `GROUPTICKET CREATE;TG01;G01;2027-04-15`, deletion, rejections and HELP output
- [x] 3.2 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with GROUPTICKET commands and verify controlled outputs
