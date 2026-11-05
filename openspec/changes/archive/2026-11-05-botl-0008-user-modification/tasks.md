# Tasks

## 1. Model and service

- [x] 1.1 Add `model.UserField` enum with the six modifiable fields and a safe lookup by name; verify a unit test that `DNI` and unknown names are not found
- [x] 1.2 Add `UserRepository.update(User)` persisting the change and `UserService.updateUser(email, password, field, value)` with authentication, validation and uniqueness; verify unit tests for each field, wrong password, invalid values, email taken by another user and restart persistence

## 2. Command

- [x] 2.1 Handle `USER UPDATE;Email;Password;Field;NewValue` in `CommandProcessor` returning `USER UPDATED` or `ERROR: ...` (DNI, unknown field, wrong element count); verify unit tests

## 3. Integration

- [x] 3.1 In a temp directory, run the JAR with the assignment's create/update examples, restart and verify `users.txt` reflects the updates; run `mvn clean package` and verify all tests pass
