# Tasks

## 1. User deletion

- [x] 1.1 Add `ParkController.deleteUser` (exists, password, ticket/group rules, group removal) and the `USER DELETE` dispatch; verify with tests for the source example, wrong password, unknown user, wrong field count, active individual ticket, member of a locked group, and group removal with a historical group ticket kept
- [x] 1.2 Verify persistence through `App.run`: the deleted user is absent after restart and the historical group ticket still reconstructs with the user's email and DNI; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run of the Delivery 1 example flow up to USER DELETE
