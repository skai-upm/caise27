# Tasks

## 1. Deletion

- [x] 1.1 Add `UserRepository.delete(dni)`; verify unit test
- [x] 1.2 Add `UserService.deleteUser(email, password)` (locate by email, check existence, check password, persist without the user, delete); verify unit tests for success, unknown email, wrong password and saved contents
- [x] 1.3 Handle `USER DELETE;Email;Password` in `CommandProcessor` (2 arguments, prints `USER DELETED`); verify unit tests for success, errors and argument count, and that the DNI/email can be reused after deletion
- [x] 1.4 Run `mvn package` and a JAR run creating then deleting a user from a temporary directory; verify `BUILD SUCCESS` and that `users.txt` no longer contains the user

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C05] Implement user deletion`; verify with `git log -1 --format=%s`
