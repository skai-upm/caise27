# Tasks

## 1. Registration

- [x] 1.1 Add `registerUser` to `ParkController` (validation, DNI and e-mail uniqueness, BCrypt protection); verify with controller tests (valid user stored with all attributes and protected password, each invalid field, duplicate DNI, duplicate e-mail, nothing stored on failure)
- [x] 1.2 Add and register `UserCreateCommand`; verify with command tests (source example, invalid data, malformed command followed by another command, persistence after restart)

## 2. Delivery

- [x] 2.1 Run `mvn clean package` (BUILD SUCCESS) and the JAR with the source example in `--fly` mode; verify `USER CREATED`
- [x] 2.2 Commit with the exact message `[F1-C03] Implement user creation`; verify with `git log -1 --format=%s`
