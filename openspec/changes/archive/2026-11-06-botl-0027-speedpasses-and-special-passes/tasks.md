# Tasks

## 1. Model

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass` and `PassRepository` (in `ParkRepositories`), and verify with unit tests for prices (incl. discounted constituents), consumed state and refundability by date

## 2. Service and cascades

- [x] 2.1 Add `TicketService.findIndividual` (top-level or constituent) and the singleton `PassService` (purchases, refunds) and verify with unit tests for every purchase/refund scenario of the `passes` spec
- [x] 2.2 Route `TICKET DELETE`/`GROUPTICKET DELETE` through `ParkController` to remove the passes of deleted tickets, and verify with unit tests that allowed deletions remove passes and rejected ones keep them

## 3. Commands and invoices

- [x] 3.1 Dispatch the four pass commands, add them to the catalog/`HELP`/README, and verify with `CommandProcessor` tests including printed totals
- [x] 3.2 Add pass sections to user invoices and verify with an invoice test (own and constituent passes, total 84.00) and that group invoices exclude passes

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass; run the JAR with a pass purchase/refund/invoice sequence and verify outputs
- [x] 4.2 Run `openspec validate botl-0027-speedpasses-and-special-passes --strict` and verify the change is valid
