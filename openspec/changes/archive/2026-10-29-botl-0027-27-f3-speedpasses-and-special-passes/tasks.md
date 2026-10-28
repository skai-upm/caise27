# Tasks

## 1. Pass model and service

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` and `PassRepository`; verify unit tests for prices (incl. discounted composition ticket) and refundability
- [x] 1.2 Create `PassService` (singleton, injected by `ParkInitializer`) with purchases, refunds and cascade deletion; verify unit tests: packs of 1/10, invalid quantity, past ticket, id conflict creating nothing, single Special Pass, refund rules
- [x] 1.3 Cascade pass deletion in `ParkController` ticket/group-ticket deletion; verify tests for cascade and for rejected deletions keeping passes

## 2. Commands and invoices

- [x] 2.1 Add the four commands to `CommandProcessor` and `CommandCatalog`; verify processor tests for responses and errors
- [x] 2.2 Add pass steps to `UserInvoiceBuilder` and include passes in `InvoiceService` user invoices; verify invoice tests incl. passes on a composition ticket not counted on the group invoice
- [x] 2.3 Run `mvn package` and a JAR session with purchases, refunds and invoices; verify responses

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F3-C06] Add speed and special passes`; verify with `git log -1 --format=%s`
