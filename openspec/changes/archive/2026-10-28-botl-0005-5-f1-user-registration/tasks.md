# Tasks

## 1. User storage and service

- [x] 1.1 Create `repository.UserRepository` (in-memory, ordered, lookup by DNI and email); verify with unit tests for save, findByDni, findByEmail and findAll
- [x] 1.2 Create `service.UserService.createUser` validating data with `UserValidator` and enforcing DNI/email uniqueness, throwing `UserOperationException` with the reason; verify unit tests for success, each invalid field, duplicate DNI and duplicate email

## 2. Command interface

- [x] 2.1 Create `cli.CommandProcessor` splitting lines on `;`, ignoring blank lines, handling `USER CREATE` (7 arguments), and returning `ERROR:` messages for unknown commands, wrong argument counts and rejected data; verify unit tests including that processing continues after errors
- [x] 2.2 Wire `App.processLine` to the processor and print its response; verify an end-to-end test running `App.execute` with a file containing a valid create, a duplicate and an unknown command, and `java -jar target/amusement-park.jar` with the same file
- [x] 2.3 Run `mvn package`; verify `BUILD SUCCESS` with all tests passing

## 3. Milestone commit

- [x] 3.1 Commit with the exact message `[F1-C03] Implement user creation`; verify with `git log -1 --format=%s`
