# Tasks

## 1. Deletion

- [x] 1.1 Add `deleteUser(email, password)` to `ParkController`; verify with controller tests (correct password deletes, wrong password and unknown user rejected, active ticket still blocks)
- [x] 1.2 Add and register `UserDeleteCommand`; verify with an integration test (create, wrong password, delete, restart shows the user gone)

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with create/delete commands in `--fly` mode; verify output
- [x] 2.2 Commit with the exact message `[F1-C05] Implement user deletion`; verify with `git log -1 --format=%s`
