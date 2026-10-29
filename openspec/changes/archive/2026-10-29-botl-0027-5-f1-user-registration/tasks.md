# Tasks

## 1. User creation

- [x] 1.1 Add `ParkController.createUser` (validation, DNI/email uniqueness, protected password) and the `USER CREATE` dispatch; verify with tests for the source example, each invalid field, duplicate DNI, duplicate email, wrong field count, and that processing continues
- [x] 1.2 Verify persistence and modes: a created user survives a restart in normal mode and leaves nothing with `--fly`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run creating a user and buying a ticket for it
