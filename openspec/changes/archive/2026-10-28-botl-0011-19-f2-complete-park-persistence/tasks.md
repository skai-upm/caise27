# Tasks

## 1. Domain support for restore

- [x] 1.1 Add `GroupTicket.restore`, controller listing getters, public `reset()` and restore operations; verify the existing test suite passes

## 2. Repositories

- [x] 2.1 Implement `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` and `ParkStorage` with the documented text formats; verify with round-trip tests (relationships, deleted user/group history, attractions empty, malformed line skipped)

## 3. Execution lifecycle

- [x] 3.1 Load at start and save after every command in `App`, with `data/` under the working directory; verify with an integration test running two executions in a temporary working directory (state survives, price resets, locked group still locked)
- [x] 3.2 Add `--fly` (timestamped `tmp/` directory, same repositories, deletion at EXIT or end of input, interactive and file forms); verify with integration tests that the directory is removed and permanent data is untouched
- [x] 3.3 Add `data/` and `tmp/` to `.gitignore`; verify with `git check-ignore data/x tmp/x`

## 4. Delivery

- [x] 4.1 Run `mvn clean package` (BUILD SUCCESS) and run the JAR in a scratch directory with and without `--fly` and a command file; verify files are created only without `--fly` and `tmp/` is cleaned
- [x] 4.2 Commit with the exact message `[F2-C08] Extend park persistence`; verify with `git log -1 --format=%s`
