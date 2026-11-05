# Tasks

## 1. Model and repository

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass`, `PassRepository`; include passes in `Repositories` and `Park`; verify unit tests for prices (incl. discounted ticket), state and identifier uniqueness

## 2. Operations

- [x] 2.1 Add singleton `PassService` (purchase 1/10 atomically, Special Pass uniqueness, refunds) and register the four commands with HELP purposes; verify command tests for all acceptance and rejection cases
- [x] 2.2 Cascade pass deletion in `TICKET DELETE`/`GROUPTICKET DELETE` without partial deletions; verify tests

## 3. Invoices and integration

- [x] 3.1 Add pass steps to the user invoice builder and director; verify user/group invoice tests; run `mvn clean package` and the JAR with a pass sequence
