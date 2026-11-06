# Proposal

## Why

Milestone F1-C09 (Delivery 1, step 11) closes Delivery 1: all features must work together, every situation during use — including input that cannot be processed — must be handled in a controlled way, and the project must build from scratch with `mvn package` into a JAR that runs without an IDE both interactively and with a command file. Two uncontrolled paths remain today: an unreadable user file at start-up ends the application with a stack trace, and an unexpected failure while processing a line would stop processing. Command-file reading also still sits in the main class, which the existing `user-management` spec forbids.

## What Changes

- Handle an unreadable user file at start-up with an error message and a normal, controlled end without processing commands (so the unreadable data is never overwritten).
- Treat any line whose processing fails unexpectedly as an incorrect command: print an error message and continue with the next line.
- Move command-source reading (standard input / command file) out of `Main` into a dedicated command-runner class; `Main` only wires components.
- Verify the complete Delivery 1 flow of source section 16 end-to-end in both modes from a clean build, and document build/run usage in the README.

### In scope
- Source sections 13 (technical restrictions), 16 (execution example), 17 (buildable Maven project, executable JAR, interactive and file mode).

### Out of scope
- Delivery 2 features. Git-history requirements of section 15 are satisfied by the project history, not by code.

## Capabilities

### New Capabilities
- `delivery-1-integration`: end-to-end Delivery 1 behavior and the technical restrictions (standard library only, no regular expressions) verified as a whole.

### Modified Capabilities
- `user-persistence`: start-up loading also covers an unreadable user file in a controlled way.
- `command-interface`: incorrect commands now include lines whose processing fails unexpectedly.

## Impact

- New `es.upm.park.command.CommandRunner`; `Main` simplified; `CommandProcessor` gains a safety net; README updated; new end-to-end tests.
