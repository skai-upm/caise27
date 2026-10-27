# Tasks

## 1. Access mechanism

- [x] 1.1 Add `Ticket.holders()` and `isValidOn(date)` (implemented in `IndividualTicket` and `GroupTicket`); verify in `TicketTest`/`GroupTicketTest`
- [x] 1.2 Implement ATTRACTION ENTER for individual tickets (validity, already-inside, capacity) and list it in HELP; verify every attraction-access scenario in `AttractionAccessTest`
- [x] 1.3 Remove deleted users from attractions on USER DELETE; verify in `AttractionAccessTest`

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F2-C06] Add individual attraction access`; verify with `git log -1 --format=%s`
