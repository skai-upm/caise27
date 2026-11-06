# Tasks

## 1. Common access mechanism

- [x] 1.1 Add the abstract `Ticket.admittedUsers()` and `Ticket.isValidOn(date)`, implemented by `IndividualTicket` (owner) and `GroupTicket` (composition owners), and verify with model tests that each returns the expected users and that validity holds only on the ticket date
- [x] 1.2 Add `ParkController.enterAttraction` (existence, date validity, already-inside, capacity, all-or-nothing), and verify with `ParkControllerAccessTest` a successful entry, wrong date, full attraction, unknown ids, already inside, and presence after a date change

## 2. Command

- [x] 2.1 Add the ATTRACTION ENTER handler and HELP line, and verify with `AttractionCommandsTest` the confirmation, the errors and the HELP content
- [x] 2.2 Document ATTRACTION ENTER in `README.md`, and verify that `mvn clean package` passes
