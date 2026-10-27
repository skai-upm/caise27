# Tasks

## 1. Update domain logic

- [x] 1.1 Add the `UserField` enum and `UserRepository.update(oldEmail, user)` and verify with `UserRepositoryTest` that re-keying by a new email works and is persisted
- [x] 1.2 Implement `UserService.updateUser` (authenticate, validate per field, uniqueness against other users, mutate, persist) and verify with `UserServiceTest`: each field update, wrong password, unknown user, invalid email/password/card, duplicate email, and own email kept

## 2. USER UPDATE command

- [x] 2.1 Dispatch `USER UPDATE;Email;Password;Field;NewValue` in `CommandProcessor` (exactly 5 elements, field parsing, `USER UPDATED` / `ERROR: ...`) and verify with `CommandProcessorTest` success, DNI rejection, unknown field and wrong element count
- [x] 2.2 Verify with `AppTest` that an update persists across runs, then run `mvn package` and the source examples through the JAR successfully
