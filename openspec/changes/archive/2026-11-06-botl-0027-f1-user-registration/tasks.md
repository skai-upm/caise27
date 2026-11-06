# Tasks

## 1. Registry

- [x] 1.1 Add the user registry, `registerUser` (validate, uniqueness, protect, store), `findUser`/`getUsers`, and the registry-backed default `UserDirectory` to `ParkController`; verify unit tests for creation, invalid data, duplicate DNI and e-mail, the protected password, and a registered user buying a ticket

## 2. Command

- [x] 2.1 Add `UserCommands` (`USER CREATE`), wire it in `App.registerCommands`, and update the UML; verify command tests for success, errors and malformed input, then run `mvn -q package` and verify all tests pass
