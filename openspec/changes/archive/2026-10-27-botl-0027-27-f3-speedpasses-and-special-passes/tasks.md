# Tasks

## 1. Pass model and storage

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` and `PassRepository`, and `TicketRepository.findIndividualTicket`; verify with `PassTest` (prices from ticket and discounted component, consumed state, refundability)

## 2. Operations

- [x] 2.1 Create `PassController` (purchases, refunds, removal by ticket) wired by `ApplicationInitializer`, plus `PassCommands` and help entries; verify the purchase and refund scenarios in `PassCommandsTest`
- [x] 2.2 Cascade pass deletion from TICKET DELETE and GROUPTICKET DELETE; verify in `PassCommandsTest`
- [x] 2.3 Add pass sections to user invoices; verify the invoices scenario in `PassCommandsTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C06] Add speed and special passes`; verify with `git log -1 --format=%s`
