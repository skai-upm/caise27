# Tasks

## 1. Access through the ticket abstraction

- [x] 1.1 Add abstract `Ticket.getHolders()` (individual: its user; group: rejected for now) and `AttractionService.enter` (ticket exists, date equals system date, attraction exists, capacity, not already inside); verify with `AttractionServiceTest`: valid entry, other date, unknown ticket/attraction, full attraction, presence after date change, already inside, group ticket rejected

## 2. Command

- [x] 2.1 Dispatch `ATTRACTION ENTER` in `CommandProcessor` and add it to HELP; verify with `CommandProcessorTest` the source example `ATTRACTION ENTER;A01;T001` and an error case, then run `mvn package` successfully
