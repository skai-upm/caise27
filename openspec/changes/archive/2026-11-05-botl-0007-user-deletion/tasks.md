# Tasks

## 1. Deletion logic

- [x] 1.1 Add `UserRepository.remove(User)` that saves to storage; verify a unit test that a new repository on the same file no longer loads the removed user
- [x] 1.2 Add `UserService.deleteUser(email, password)` with not-found and wrong-password rejections; verify unit tests for success, unknown email and wrong password (user kept)

## 2. Command

- [x] 2.1 Handle `USER DELETE;Email;Password` in `CommandProcessor` returning `USER DELETED` or `ERROR: ...` (including wrong element count); verify unit tests, including re-creating a deleted user

## 3. Integration

- [x] 3.1 In a temp directory, create Ana and Pedro with the JAR, delete Pedro, try deleting Ana with a wrong password, restart and verify only Ana remains in `users.txt`; run `mvn clean package` and verify all tests pass
