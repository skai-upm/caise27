# Tasks

## 1. User storage and service

- [x] 1.1 Create `repository.UserRepository` (in-memory: add, findByDni, findByEmail, findAll, count); verify unit tests for add and lookups
- [x] 1.2 Create `service.UserService.createUser` validating with `UserValidator` and checking DNI/email uniqueness, throwing `UserOperationException` on rejection; verify unit tests for success, each invalid field, duplicate DNI and duplicate email leave the repository unchanged

## 2. Command interface

- [x] 2.1 Create `command.CommandProcessor` that splits lines on `;` with `split`, trims elements, ignores blank lines, dispatches `USER CREATE` and returns `USER CREATED` or `ERROR: ...`; verify unit tests for valid, invalid, wrong argument count and unknown commands
- [x] 2.2 Make `InteractiveSession` pass each line to `CommandProcessor` and print non-empty responses; wire it in `App`; verify `InteractiveSessionTest` captures `USER CREATED` output and continues after an error

## 3. Integration

- [x] 3.1 Run `mvn clean package` and pipe a `USER CREATE` sequence (valid, duplicate DNI, duplicate email, invalid DNI, unknown command) into `java -jar target/amusement-park.jar`; verify the expected responses and exit status 0
