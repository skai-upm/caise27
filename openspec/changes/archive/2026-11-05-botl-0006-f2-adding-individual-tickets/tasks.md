# Tasks

## 1. Ticket model

- [x] 1.1 Create abstract `Ticket` and `IndividualTicket`; verify unit tests for price, active-on and deletable-on rules

## 2. Central management

- [x] 2.1 Add ticket registry with shared identifier check, `createIndividualTicket`, `deleteTicket` and `ensureUserDeletable` to `ParkController` (cleared by `reset`); verify unit tests for all positive and negative scenarios

## 3. Commands

- [x] 3.1 Implement `TICKET CREATE` / `TICKET DELETE` commands, register in `App.run`, list in `HELP`; verify command tests including the displayed price and malformed input

## 4. Verification

- [x] 4.1 Run `mvn -q package` and verify all tests pass
