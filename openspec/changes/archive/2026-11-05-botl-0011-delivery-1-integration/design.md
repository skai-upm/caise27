# Design

## Context

Delivery 1 features are implemented (see specs). `UserOperationException` covers expected rejections; other runtime exceptions (e.g. `UncheckedIOException` from `UserFileStorage`) currently propagate out of the session.

## Goals / Non-Goals

**Goals:** no uncontrolled termination; restriction compliance verified by tests. **Non-Goals:** new commands or format changes.

## Decisions

- **Catch-all in `CommandProcessor.process`**: after the `UserOperationException` handler, catch `RuntimeException` and return `ERROR: the command could not be processed (<reason>)`. Keeping it in the processor guarantees identical handling for both input sources.
- **Start-up guard in `App.run`**: building the repository (which loads the file) is wrapped; an `UncheckedIOException` prints `ERROR: cannot read users file '<path>'` to standard error and returns 1.
- **Email validation without regex**: `split("@", -1)` (literal single character) for the local/domain split; `indexOf('.')`/`lastIndexOf('.')` and `substring` for `text.xxx` with exactly one dot. Behavior identical to before.
- **Compliance test**: a unit test scans `src/main/java` for forbidden constructs (`java.util.regex`, `.matches(`, `replaceAll(`, `replaceFirst(`, `split("\\`, `javax.crypto`, `MessageDigest`) — cheap, keeps later deliveries honest.

## Risks / Trade-offs

- [Catch-all may hide programming errors] → the message includes the exception's message; tests cover expected paths explicitly.
