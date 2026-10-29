# Tasks

## 1. Common access abstraction

- [x] 1.1 Add abstract `admittedUsers()` to `Ticket`, implemented by `IndividualTicket` (its user) and `GroupTicket` (composition users); verify with unit tests for both types

## 2. Entry operation

- [x] 2.1 Add `ParkController.enterAttraction` (exists checks, composed/group tickets rejected, date equals current date, room for all and none already inside, all-or-nothing); verify with unit tests for today's ticket, other dates, full attraction, unknown attraction/ticket, duplicate entry, and presence after a later date change
- [x] 2.2 Dispatch `ATTRACTION ENTER` in `CommandProcessor` and add it to HELP; verify with processor tests for `ATTRACTION ENTER;A01;T001` followed by `ATTRACTION LIST;A01`, rejections and HELP output
- [x] 2.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with `ATTRACTION CREATE;A01;Dragon Rojo;20`, `ATTRACTION ENTER;A01;T001`, `EXIT` and verify the controlled error
