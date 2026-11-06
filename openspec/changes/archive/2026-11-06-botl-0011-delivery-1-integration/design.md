# Design

## Context

See proposal.md – Why. `Main` currently wires components, opens the command file or standard input and runs the shared loop. `UserRepository(UserFileStore)` loads in its constructor and throws `PersistenceException` on read failure. `CommandProcessor.process` catches `UserException` and `PersistenceException` only.

## Goals / Non-Goals

**Goals:**
- No uncontrolled termination on any input or data-file problem.
- Entry point limited to wiring, as `user-management` requires.

**Non-Goals:**
- New commands or Delivery 2 entities.

## Decisions

- **`CommandRunner` (package `es.upm.park.command`)**: owns the single loop (`run(BufferedReader)`), `runFile(String path)` and `runStandardInput(InputStream)`, writing to a `PrintStream`. It contains the EXIT check and the controlled messages for unreadable sources moved from `Main`. Alternative: keep it in `Main` — rejected by the existing entry-point rule.
- **Start-up load failure**: `Main` catches `PersistenceException` from creating the repository, prints `Error: cannot load users: <reason>` and returns before reading any command; continuing with an empty list could overwrite the unreadable file on the next save.
- **Safety net**: `CommandProcessor.process` additionally catches `RuntimeException` after the specific handlers and returns `Error: command could not be processed`. Known errors keep their specific messages.
- **README**: build (`mvn package`) and run (`java -jar target/amusement-park.jar [input.txt]`) instructions, commands summary.

## Risks / Trade-offs

- [Catching `RuntimeException` could hide bugs] → only at the per-line boundary, with a visible error message; tests cover the specific paths.
