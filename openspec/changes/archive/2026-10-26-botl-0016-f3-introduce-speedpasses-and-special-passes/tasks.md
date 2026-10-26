# Tasks

## 1. Pass model

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` and add pass ownership to `IndividualTicket` (multiple SpeedPasses, at most one Special Pass, consume keeps the pass); verify unit tests for prices, uniqueness of the Special Pass and consumption

## 2. Rules, persistence and commands

- [x] 2.1 Create `PassRepository` and `FilePassRepository` (`passes.txt`); verify a round-trip test restoring kind, price and consumed status onto tickets
- [x] 2.2 Create Singleton `PassController` (buy 1/10 SpeedPasses, buy Special Pass, refunds with the E2 date rule and unused check, ticket must be an individual ticket dated ≥ system date) and block deleting tickets with passes in `TicketController`; verify unit tests for each rule
- [x] 2.3 Add the four commands to `CommandProcessor` and `CommandCatalog`, wire `PassController` in `App.initialize`; verify command tests (amounts shown, invalid quantity, group ticket rejected, internal ticket accepted, refund rules) and that `--fly` stores passes in the temporary directory

## 3. Invoice and model

- [x] 3.1 Add the "Passes" section to user invoices (passes of direct and internal tickets); verify the invoice test totals 48.00 for T001 + a SpeedPass on TG01-1
- [x] 3.2 Update `uml/model.puml`; verify `plantuml -checkonly`, the model/code cross-check and `mvn test` pass
