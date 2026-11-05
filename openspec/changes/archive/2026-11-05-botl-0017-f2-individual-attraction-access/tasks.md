# Tasks

## 1. Common abstraction

- [x] 1.1 Add abstract `Ticket.getHolders()` implemented by `IndividualTicket` (its user) and `GroupTicket` (composition users); verify a test for both types

## 2. Access command

- [x] 2.1 Implement `ATTRACTION ENTER` in `AttractionCommands` (attraction/ticket lookup, temporary group-ticket guard, date = system date, entrants not already inside, capacity, all-or-nothing admission) and list it in `HELP`; verify `AttractionAccessTest` for every scenario in the attractions delta (valid entry, future/past ticket, full attraction, unknown attraction/ticket, already inside, presence after date change, group ticket rejected, HELP)

## 3. Integration

- [x] 3.1 End-to-end with the JAR: date, user, ticket, attraction with capacity 1, entry, second user rejected for capacity, list, date change keeps presence; verify responses and exit status 0; full `mvn package` regression
