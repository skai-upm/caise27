# Tasks

## 1. Central management and settings

- [x] 1.1 Generalise `UserOperationException` to `OperationException`; split `UserService.delete` into `authenticate` + `remove`; verify existing tests (updated) pass
- [x] 1.2 Add `validation.ValueParser` (strict date, price ≤2 decimals > 0, positive capacity; no regex); verify `ValueParserTest`
- [x] 1.3 Add `persistence.DataPaths` and the `service.Park` singleton (open/reset, date, price, collections, queries, save); add `SYSTEM DATE` / `SYSTEM PRICE` commands; verify `ParkTest` (single instance, initial date/price, reset on open) and command tests

## 2. Groups

- [x] 2.1 Add `model.Group`, `persistence.GroupFileRepository`, `service.GroupService` (create, add, remove, delete with derived locking) and `GROUP *` commands; verify `GroupServiceTest` (duplicates, unknown user/group, locked/unlocked, delete keeps historical tickets with null group)

## 3. Tickets

- [x] 3.1 Add `model.Ticket`, `IndividualTicket`, `GroupTicket`, `GroupDiscount`; verify `TicketModelTest` (prices, discounts brackets, active/deletable/valid, holders)
- [x] 3.2 Add `service.TicketService` (TICKET CREATE/DELETE, GROUPTICKET CREATE/DELETE, shared id space incl. derived ids, atomic creation) and commands; verify `TicketServiceTest` including 6-member 192.00 example and component deletion rejection
- [x] 3.3 Add `Park.deleteUser` (active tickets, locked groups, removal from groups) wired to `USER DELETE`; verify `ParkUserDeletionTest` (Marta example, locked group, historical tickets kept)

## 4. Attractions

- [x] 4.1 Add `model.Attraction`, `service.AttractionService` (create, enter all-or-nothing, exit, list, status) and commands; verify `AttractionServiceTest` (capacity, group entry, date validity, already inside, exit, list empty, status content, date change keeps presence)

## 5. Persistence

- [x] 5.1 Add `persistence.TicketFileRepository` and `AttractionFileRepository`; make `Park.open/save` load and save all entities; verify `ParkPersistenceTest` (restart round-trip, attractions empty, deleted user's and deleted group's historical tickets reconstructed)
- [x] 5.2 Add `--fly` handling in `App` (timestamped `tmp/` dir, same repositories, cleanup in all endings), switch to `park.data.dir`; verify `AppTest` (fly with file, fly interactive, normal data untouched, directory removed)

## 6. Documentation

- [x] 6.1 Extend HELP (generated from the command registry) and README (commands, data files, `--fly`); verify `CommandProcessorTest` checks every syntax line from the specification
- [x] 6.2 Write `uml/model.puml` covering classes, enum, inheritance, composition, directed associations and multiplicities matching the code; verify by cross-checking class/field names against the sources

## 7. Integration

- [x] 7.1 Run `mvn package`, then run the specification's Delivery 2 example file normally and with `--fly`; verify outputs (192.00/108.00-style totals, Marta deletion after T002 delete, G02 deletion after TG02 delete), that `tmp/` is cleaned and that Delivery 1 commands still work
