# Tasks

## 1. Group entry

- [x] 1.1 Remove the group-ticket guard in `enterAttraction` so group tickets use the same checks; verify with unit tests for a group entering together, insufficient capacity (nobody enters), a member already inside (nobody enters) and a wrong date

## 2. Exit and status

- [x] 2.1 Add `Attraction.getAvailablePlaces()` and `ParkController.exitAttraction` (by email among occupants); verify with unit tests for one group member leaving, place released, user not inside and unknown attraction
- [x] 2.2 Dispatch `ATTRACTION EXIT` and `ATTRACTION STATUS` in `CommandProcessor` (status shows id, name, capacity, inside count, available places, occupants) and add both to HELP; verify with processor tests for status after mixed individual/group entries and an exit, and HELP output
- [x] 2.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with `ATTRACTION CREATE;A01;Dragon Rojo;20`, `ATTRACTION STATUS;A01`, `ATTRACTION EXIT;A01;ana@correo.com`, `EXIT` and verify the outputs
