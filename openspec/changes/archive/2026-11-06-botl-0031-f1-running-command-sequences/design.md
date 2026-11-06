# Design

## Context

`App` reads standard input when no command file is given and the file otherwise. Both feed the same `CommandRunner` and `CommandProcessor`. Blank lines are skipped in the runner, and unreadable files are reported by `CommandRunner.runFile`.

## Goals / Non-Goals

**Goals:** prove the milestone with its own example session, now that USER commands exist.
**Non-Goals:** changes to input handling.

## Decisions

- **Verification only, `skip_specs: true`**: the behavior is already canonical, and duplicating requirements is forbidden by the request.
- **`CommandSequencesTest`** runs `App.run` twice on fresh temporary directories, once with the example on stdin and once with it in a file, and compares the outputs. It also checks the order of confirmations and the controlled message for a missing file.

## Risks / Trade-offs

- [None of the code changes] → Intended. The test guards the milestone against regressions.
