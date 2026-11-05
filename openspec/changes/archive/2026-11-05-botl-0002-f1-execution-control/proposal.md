# Proposal

## Why

Milestone F1-C08 (Delivery 1, step 10) requires the general `HELP` and `EXIT` commands: `HELP` documents the available commands and `EXIT` terminates the application in a controlled way, behaving identically for standard input and command files, and preventing any later command from running. The current baseline (archetype `App` printing "Hello World!") has no command processing at all, so these commands have nowhere to live yet.

## What Changes

- Add `HELP`: prints the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP`, `EXIT`.
- Add `EXIT`: stops command processing in a controlled manner; no further line is processed after it, whatever the input source.
- Introduce the minimal enabling infrastructure these commands need: one shared line-by-line command processor used by both input sources (standard input when started without arguments; the file given as first argument otherwise), skipping blank lines and reporting unrecognized commands without stopping.
- Replace the archetype "Hello World!" behavior of the entry point with this processor.

### In scope
- `HELP` and `EXIT` semantics; a single processing loop shared by stdin and file input so both behave identically.

### Out of scope
- `USER` commands themselves (they are only documented by `HELP`), executable-JAR packaging (F0-C02), full file-mode error policy beyond a controlled error message (F1-C07), persistence.

### Open question
- The source says `HELP` shows "the operations currently available" yet requires listing `USER` commands at a minimum. Until those commands exist, `HELP` lists them as documented syntax as the source mandates; this is noted, not resolved by inventing a rule.

## Capabilities

### New Capabilities
- `execution-control`: General session commands `HELP` and `EXIT`, and the uniform line-by-line processing they rely on across input sources.

### Modified Capabilities
<!-- None: project-initialization requirements are unchanged. -->

## Impact

- `src/main/java/es/upm/amusementpark/App.java` (entry point now runs the command processor).
- New command-processing classes and unit tests under `src/main/java` / `src/test/java`.
- No persistence or dependency changes.
