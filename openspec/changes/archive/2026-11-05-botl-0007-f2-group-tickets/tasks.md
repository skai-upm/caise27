# Tasks

## 1. Model

- [x] 1.1 Add `GroupTicket` (components, nullable group, summed price, discount brackets) and the containing-group link on `IndividualTicket`; verify unit tests for the discount brackets and the 6×32=192 example

## 2. Central management

- [x] 2.1 Add `UserDirectory.dniOf`, atomic `createGroupTicket`, `deleteGroupTicket`, composition-aware `deleteTicket`, a lock computed from real group tickets, and clearing of the group reference on group deletion; verify unit tests for each scenario, including the no-partial-creation conflict
- [x] 2.2 Extend `ensureUserDeletable` with the locked-group check and add `removeUserFromAllGroups`; verify unit tests

## 3. Commands

- [x] 3.1 Implement `GROUPTICKET CREATE/DELETE`, register them, and list them in `HELP`; verify command tests show the total price and errors

## 4. Verification

- [x] 4.1 Run `mvn -q package` and verify all tests pass
