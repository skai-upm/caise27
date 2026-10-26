# Tasks

## 1. Model and persistence

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` (pricing, consumption); verify with unit tests for 20% and 100% prices and consumption status
- [x] 1.2 Create `PassRepository` (`type;id;ticketId;price;consumed`); verify with a `@TempDir` round-trip test including consumed status and an unknown ticket

## 2. Rules

- [x] 2.1 Create `PassController` (buy 1/10 SpeedPasses, buy Special Pass once, eligibility, refunds with date rule, hasPasses); verify with unit tests for every spec scenario

## 3. Integration

- [x] 3.1 Introduce `BusinessControllers`, add the four pass commands to `CommandProcessor` and the catalog (with descriptions), block ticket/group-ticket deletion when passes exist, wire `PassController` and `passes.txt` in `App`; verify with command tests
- [x] 3.2 Add the personal passes section to user invoices; verify with invoice tests (including a pass on a group-ticket ticket)
- [x] 3.3 Run `mvn test`, the regression script and a jar session with passes and a restart; verify outputs; update `uml/model.puml` and verify with `plantuml -checkonly`
