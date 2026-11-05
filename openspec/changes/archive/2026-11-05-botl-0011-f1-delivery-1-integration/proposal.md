# Proposal

## Why

Milestone F1-C09 (Delivery 1, step 11) closes Delivery 1: all features must work together, and input that cannot be processed correctly must be handled in a controlled manner. The project must also build from scratch with Maven, and the JAR must run without an IDE both interactively and with a command file (source §13, §16, §17). An audit of the current state found that start-up crashes with an uncaught exception when the users file exists but cannot be read. It also found that lines in the users file with the right field count but invalid or duplicated data are loaded unchecked, bypassing the invariants that `USER CREATE` enforces. Finally, nothing guards the "standard library only" restriction or exercises the §16 end-to-end flow.

## What Changes

- Revalidate users on load: a stored line is loaded only when its data satisfies the user-validation rules and its DNI and email are not already used by an earlier loaded line. Other lines are ignored, and the application reports how many were ignored.
- Handle an unreadable users file at start-up in a controlled manner: print an `ERROR` naming the file and terminate without an uncaught exception, executing no command. This prevents overwriting data that could not be read.
- Add an integration test that runs the §16 command sequence, in both file mode and interactive mode, against a file-backed store and checks the resulting users.
- Add guard tests for the technical restrictions: no runtime (non-test) Maven dependencies, and no regular-expression APIs in main sources.
- Document how to build and run the application in `README.md`.

In scope: the hardening and verification above.

Out of scope: Delivery 2 features, and changing output wording (the source allows different but equivalent messages).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `user-persistence`: adds requirements that loaded users satisfy the same validity and uniqueness rules as created users, and that an unreadable users file is handled in a controlled manner.
- `project-initialization`: adds the technical restriction that the application uses only the Java standard library at runtime and no regular expressions.

## Impact

- `UserFileStore`/`UserRepository` load path, `App` start-up error handling, new integration and guard tests, `README.md`.
