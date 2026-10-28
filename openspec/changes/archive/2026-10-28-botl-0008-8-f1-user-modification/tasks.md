# Tasks

## 1. Update support

- [x] 1.1 Create `model.UserField` enum with the six modifiable fields and name lookup; verify unit test that exactly those six exist and `DNI`/unknown names are not found
- [x] 1.2 Add `UserService.updateUser` (authenticate, validate per field incl. email uniqueness, copy-then-persist); verify unit tests for each field, invalid values, duplicate email, wrong password and saved contents
- [x] 1.3 Handle `USER UPDATE;Email;Password;Field;NewValue` in `CommandProcessor` (4 arguments, unknown/DNI field rejected, prints `USER UPDATED`); verify unit tests
- [x] 1.4 Run `mvn package` and a JAR run with the statement's update examples from a temporary directory; verify `BUILD SUCCESS` and `users.txt` reflects the updates

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C06] Implement user update`; verify with `git log -1 --format=%s`
