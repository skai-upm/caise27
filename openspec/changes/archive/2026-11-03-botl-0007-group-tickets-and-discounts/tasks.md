# Tasks

## 1. Ticket abstraction and group ticket

- [x] 1.1 Create abstract `ParkTicket` (id, date, getPrice) and make `Ticket` extend it; verify existing ticket tests still pass
- [x] 1.2 Create `GroupTicket` (group aggregation + groupId, unit price, composed constituent tickets `<id>-<n>`, discount brackets, discounted total); verify `GroupTicketTest` for 2/3/5/6/10/11 members (80.00/108.00/180.00/192.00/320.00/220.00) and the frozen composition

## 2. Service rules

- [x] 2.1 Add `createGroupTicket`, `deleteGroupTicket`, `isGroupLocked` to `TicketService`, cross-kind id uniqueness and constituent tickets in `hasActiveTickets`; verify `GroupTicketServiceTest` (nonexistent/empty group, past date, duplicate ids, deletion boundaries with cascade, locks on and after the date)

## 3. Persistence

- [x] 3.1 Create `GroupTicketFileStorage` (format `Id;IdGrupo;fecha;total;unitario;DNIs`, link group and users, keep historical identifiers) and save after changes with revert; verify `GroupTicketFileStorageTest` exact line and round trip

## 4. Commands

- [x] 4.1 Add `GROUPTICKET CREATE` (response `OK GROUP TICKET CREATED -> <Precio>`) and `GROUPTICKET DELETE` to `CommandProcessor` and HELP; reject `GROUP ADD/REMOVE/DELETE` on locked groups; wire storage in `App`; add `grouptickets.txt` to `.gitignore`; verify command tests including the 15 HELP entries and the source examples `TG01`/`TG001`

## 5. UML

- [x] 5.1 Update `uml/model.puml` with `ParkTicket` inheritance, `GroupTicket` aggregation with `Group` and composition of `Ticket`, and `GroupTicketFileStorage`; verify `plantuml -checkonly`

## 6. Integration verification

- [x] 6.1 Verify restart and date progression: an `AppTest` buys a group ticket, restarts, checks lock and user-deletion restriction, advances `SYSTEM DATE` past the ticket and checks the group can be modified and members deleted; then `mvn clean test` passes
