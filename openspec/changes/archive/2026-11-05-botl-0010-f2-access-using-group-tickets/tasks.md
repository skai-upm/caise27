# Tasks

## 1. Group access

- [x] 1.1 Add tests for group-ticket entry: whole group admitted, insufficient capacity admits nobody, ticket not valid today rejected; verify they pass with the shared access operation

## 2. Exit and status

- [x] 2.1 Add `Attraction.getAvailablePlaces` and `ParkController.leaveAttraction`; verify unit tests for leaving, members leaving one by one, and a user not inside
- [x] 2.2 Implement the `ATTRACTION EXIT` and `ATTRACTION STATUS` commands and list them in `HELP`; verify command tests show all required status fields before and after exits

## 3. Verification

- [x] 3.1 Run `mvn -q package` and verify all tests pass
