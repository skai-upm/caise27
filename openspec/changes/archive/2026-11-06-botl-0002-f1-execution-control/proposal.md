# Proposal

## Why

Milestone F1-C08 (Delivery 1) requires the general `HELP` and `EXIT` commands. The current code is only the archetype placeholder, which prints "Hello World!". It does not read commands at all, so users can neither discover the command syntax nor stop the application in a controlled way. EXIT must behave the same in interactive and file input and must prevent any later command from running.

## What Changes

- Add a single command-processing loop that reads one command per line from an input source and writes results to standard output. Interactive input (standard input) and a command file given as the first program argument share this loop.
- Add the `HELP` command, which lists the available commands and their basic syntax. It covers at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.
- Add the `EXIT` command, which stops processing in a controlled way: no further line is read or executed, and the program terminates normally.
- Report unrecognized input lines with an error message without terminating.

### In scope
- HELP, EXIT, and the minimal shared input loop (stdin, or a file path argument) that is needed to show identical behavior in both sources.

### Out of scope
- USER commands and their behavior (later milestones; HELP only documents their syntax as the source mandates).
- Executable-JAR packaging (F0-C02).
- Detailed file-mode rules such as blank-line handling and nonexistent/unreadable files (F1-C07). A file that cannot be opened only gets a basic error message here.

## Capabilities

### New Capabilities
- `execution-control`: general commands that control the application session. HELP describes the available commands and EXIT ends the session, with the same behavior for interactive and file input.

### Modified Capabilities
- None (`project-initialization` is unaffected).

## Impact

- Code: `es.upm.park.App` (entry point now runs the command loop) and new command-processing classes under `es.upm.park`.
- Tests: new JUnit 5 tests for HELP, EXIT and the loop.
- No persistence or dependency changes.
