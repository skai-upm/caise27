# Tasks

## 1. Model

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` (price from ticket, consumed flag) and store the discounted share in constituent tickets; verify `PassTest` (8.00 / 40.00 / 7.20 / 36.00) and updated `GroupTicketTest`

## 2. Controller and persistence

- [x] 2.1 Create `PassRepository`/`PassFileStorage` and singleton `PassController` (qty 1/10, derived ids checked before mutation, one Special Pass per ticket, refunds only unconsumed and future), `TicketController.findIndividualTicket`, wiring in `App`; add `passes.txt` to `.gitignore`; verify `PassControllerTest` and `PassFileStorageTest`

## 3. Commands and invoices

- [x] 3.1 Add the four pass commands to `ParkController`, `CommandProcessor` and `CommandCatalog`; add the personal-pass invoice step; verify `PassCommandsTest` (all brief scenarios, restart) and the invoice scenario with a pass on a constituent ticket

## 4. UML and verification

- [x] 4.1 Update `uml/model.puml`; verify `plantuml -checkonly`, `mvn clean test`, `mvn package` and the regression script
