# Tasks

## 1. User store

- [x] 1.1 Create `repository.UserRepository` (in-memory, DNI-keyed, insertion order) with `add`, `findByDni`, `existsByDni`, `existsByEmail`, `findAll`, `size`; verify with `UserRepositoryTest` (add/find, exists checks, order)

## 2. Command processing

- [x] 2.1 Create `command.CommandProcessor#process(String)` parsing with `split(";", -1)` and trimming; blank lines return `null`; unknown/unsupported command names return `ERROR: ...`; unexpected exceptions become `ERROR`; verify with tests for blank, unknown (`FOO;bar`) and `USER DELETE;12345678Z`
- [x] 2.2 Implement `USER CREATE` in the processor: element count check, validation via `UserValidator` naming the invalid field, DNI then email uniqueness, store and return `OK: ...`; verify tests for success, wrong count, each invalid field, duplicate DNI, duplicate email (store unchanged)

## 3. Entry point and integration

- [x] 3.1 Replace `App.main` placeholder with a loop reading `System.in` lines, delegating to `CommandProcessor` and printing non-null responses; update `AppTest`; verify `mvn test`
- [x] 3.2 Verify end-to-end with `mvn package` and piping a command sequence (valid create, unknown command, duplicate DNI, valid create) into `java -jar target/amusement-park.jar`: responses in order, exit status 0
