# Tasks

## 1. Deletion service

- [x] 1.1 Implement `UserService.deleteUser` (locate, exists, password check, delete) and verify with `UserServiceTest`: successful deletion, unknown email and incorrect password (user kept)

## 2. USER DELETE command

- [x] 2.1 Dispatch `USER DELETE;Email;Password` in `CommandProcessor` (exactly 3 elements, `USER DELETED` / `ERROR: ...`) and verify with `CommandProcessorTest` success, wrong password, unknown user and wrong element count, and that USER CREATE/HELP/EXIT still pass
- [x] 2.2 Verify with `AppTest` that a deleted user is removed from `data/users.txt` and absent in a following run (can be re-created), then run `mvn package` successfully
