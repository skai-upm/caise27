# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `model/Ticket` and `model/IndividualTicket`; verify with `TicketTest` (active/deletable rules around the current date, price captured at creation, `involves`, `User` has no ticket field)
- [x] 1.2 Create `repository/TicketRepository` with the common `isIdAvailable` check and expose it plus `hasActiveTicket(User)` from `ParkController` (reset on `start`); verify with `TicketRepositoryTest`

## 2. Commands

- [x] 2.1 Implement TICKET CREATE and TICKET DELETE (new `TicketCommands`, shared date parsing with SYSTEM DATE) and list them in HELP; verify every tickets scenario in `TicketCommandsTest`
- [x] 2.2 Reject USER DELETE when the user has an active ticket; verify the user-deletion scenarios in `TicketCommandsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F2-C03] Add individual tickets`; verify with `git log -1 --format=%s`
