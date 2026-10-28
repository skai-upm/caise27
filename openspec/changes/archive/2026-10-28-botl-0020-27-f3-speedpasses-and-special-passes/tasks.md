# Tasks

## 1. Model and pricing

- [x] 1.1 Add `Pass`, `SpeedPass`, `SpecialPass` (price from the ticket's stored price, consumed state, refundable rule); verify with unit tests (8.00/40.00 from a 40.00 ticket, 6.40 from 32.00, consume, refundable by date/state)

## 2. Controller operations

- [x] 2.1 Add pass registry with `buySpeedPasses`, `buySpecialPass`, `refundSpeedPass`, `refundSpecialPass`; verify with controller tests (quantity 1/10, naming, all-or-nothing conflicts, past/unknown tickets, one Special Pass per ticket incl. consumed, refund rules, type mismatch)
- [x] 2.2 Cascade pass removal in `deleteIndividualTicket` and `deleteGroupTicket` with checks before any mutation; verify with controller tests (passes of two purchases removed, composition passes removed, rejected deletion leaves passes)

## 3. Integration

- [x] 3.1 Add `PassRepository`/`TextPassRepository` to the injected repositories and load/save passes; verify with persistence round-trip test (ids, prices, tickets, consumed)
- [x] 3.2 Add pass sections to user invoices (passes on direct and composed tickets) leaving group invoices unchanged; verify with invoice tests
- [x] 3.3 Add the four commands, catalogue entries and UML updates; verify with command tests, HELP tests, `UmlModelTest` and the PlantUML syntax check

## 4. Delivery

- [x] 4.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with a pass scenario in `--fly` mode; verify outputs
- [x] 4.2 Commit with the exact message `[F3-C06] Add speed and special passes`; verify with `git log -1 --format=%s`
