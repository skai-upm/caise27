# Tasks

## 1. Store support

- [x] 1.1 Add `UserRepository.findByEmail` and `remove(dni)` (persisting after removal); verify `UserRepositoryTest`: find by email, remove updates size and file, removed user absent after reload

## 2. Command

- [x] 2.1 Implement `USER DELETE;Email;Password` in `CommandProcessor` (element count, find by email, password check, remove, `OK`/`ERROR`); verify `CommandProcessorTest`: success, wrong password (user unchanged), unknown email, wrong element count, DNI/email reusable after deletion
- [x] 2.2 Update the existing not-yet-supported-command test to use `USER UPDATE` and verify `mvn test` passes

## 3. Integration

- [x] 3.1 End-to-end in a temp directory with the JAR: create a user, attempt delete with wrong password (rejected, file unchanged), delete with correct password (OK, line removed), restart and verify the user is not loaded (re-create succeeds); exit status 0
