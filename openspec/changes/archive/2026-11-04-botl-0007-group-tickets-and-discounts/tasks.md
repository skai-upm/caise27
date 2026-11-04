# Tasks

## 1. Ticket abstraction

- [x] 1.1 Rename `Ticket` to `IndividualTicket` and introduce abstract `Ticket` (id, date, abstract price); verify `mvn test` still passes unchanged behaviour

## 2. Group tickets

- [x] 2.1 Create `GroupTicket` (group aggregation, constituent composition, discount brackets, total) ; verify with `GroupTicketTest` for 1, 2, 3, 5, 6, 10, 11 members and price 60.00
- [x] 2.2 Create `GroupTicketFileStorage` (`id;groupId;date;price;total;dni...`, history for missing group/users, malformed skipped); verify with `GroupTicketFileStorageTest`
- [x] 2.3 Extend `TicketManager` with group tickets (single id space, date ≥ today, nonempty group, delete only if date > today with constituents, `hasGroupTicketOnOrAfter`, constituents in `hasTicketOnOrAfter`, constituents not deletable via `delete`); verify with `TicketManagerTest`

## 3. Commands

- [x] 3.1 Add `GROUPTICKET CREATE` (exact `OK GROUP TICKET CREATED -> <Precio>`) and `GROUPTICKET DELETE`, group-lock checks for `GROUP ADD/REMOVE/DELETE`, HELP entries, and wire `group-tickets.txt` in `App`; verify with `CommandProcessorTest` (purchase, rejections, lock/unlock, member deletion protection, composition fixed) and an `AppTest` restart

## 4. Model and regression

- [x] 4.1 Update `docs/uml/model.puml` with the ticket hierarchy, aggregation and composition; verify with the PlantUML `-syntax` check and class existence
- [x] 4.2 Run `mvn clean package` with all tests passing and a manual JAR run of a group purchase, lock and deletion
