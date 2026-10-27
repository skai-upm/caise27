# Tasks

## 1. User storage

- [x] 1.1 Create `repository/UserRepository` (in-memory, keyed by DNI) with add/find/exists by DNI and email; verify with `UserRepositoryTest`

## 2. Command processing and USER CREATE

- [x] 2.1 Create `command/CommandProcessor` splitting lines on `;`, dispatching `USER CREATE` and answering `ERROR: Unknown command` otherwise; verify unknown/blank-keyword cases in `CommandProcessorTest`
- [x] 2.2 Implement USER CREATE: seven non-empty fields, validation via `UserValidator`, DNI and email uniqueness, `USER CREATED` on success; verify every scenario of the user-registration spec in `CommandProcessorTest`
- [x] 2.3 Wire `Main` to send every line to the processor and print responses to standard output; verify `MainTest` for stdin and file modes and run `java -jar target/nombre-aplicacion.jar` with a sample file printing `USER CREATED`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F1-C03] Implement user creation`; verify with `git log -1 --format=%s`
