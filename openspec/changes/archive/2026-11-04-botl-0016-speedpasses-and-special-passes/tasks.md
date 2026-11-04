# Tasks

## 1. Model and pricing

- [x] 1.1 Create `Pass`, `SpeedPass`, `SpecialPass` (price from the ticket's stored price, consumed status); store the discounted share in group-ticket constituents (`GroupTicket.shareFor`); verify `PassTest` (8.00 / 40.00, 7.20 / 36.00 on a discounted constituent) and updated group ticket tests

## 2. Service and persistence

- [x] 2.1 Implement `PassRepository` (`Tipo;Identificador;Ticket;Precio;Consumido`) and `PassService` (SpeedPass 1/10 with derived ids and no partial purchase, Special Pass uniqueness incl. consumed, refunds by date and consumption, cascade removal); verify `PassServiceTest` incl. restart with consumed status

## 3. Controller, commands, invoices

- [x] 3.1 Inject `PassService` into `ParkController` (via `ApplicationContext`), add pass operations and ticket-deletion cascades, the four commands with `OK: SPEEDPASS CREATED -> …` / `OK: SPECIALPASS CREATED -> …`, and catalog entries; verify `PassCommandsTest`
- [x] 3.2 Add the personal passes builder step and section for user invoices (not for groups); verify invoice tests for the pass-on-constituent scenario and unchanged group totals

## 4. Documentation and regression

- [x] 4.1 Update `uml/model.puml` (pass hierarchy, ticket association, service/repository, builder step) and verify `UmlModelTest`, `LayeringTest`, `plantuml -checkonly`, `mvn package`, and that the F3 regression script output is unchanged except HELP lines and the constituent share in `tickets.txt`
