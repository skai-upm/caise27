# Tasks

## 1. Repository and service

- [x] 1.1 Add `UserRepository.findByEmail` and `remove` (persist the list without the user before updating memory) and verify with unit tests that a removed user is gone after reloading the file and that a failed save keeps the user
- [x] 1.2 Add `UserService.delete(email, password)` (locate by email, verify existence, check password, remove) and verify with unit tests: correct credentials delete; wrong password and unknown email throw `UserException` with stored users unchanged

## 2. Command

- [x] 2.1 Dispatch `USER DELETE;Email;Password` in `CommandProcessor` (2 fields, `User deleted: <email>`) and verify with unit tests for success, wrong password, unknown email, wrong field count, and re-creating a deleted user's DNI/email

## 3. Integration

- [x] 3.1 Run `mvn package` and verify all tests pass; run the JAR twice in a temp directory (create Ana; then delete with wrong password, delete with correct password) and verify `users.txt` is empty at the end
- [x] 3.2 Run `openspec validate botl-0007-user-deletion --strict` and verify the change is valid
