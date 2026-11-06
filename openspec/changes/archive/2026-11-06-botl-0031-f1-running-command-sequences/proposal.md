# Proposal

## Why

Milestone F1-C07 requires running a sequence of commands from a file whose path is given at startup, in addition to interactive input. The observable behavior of every command must be the same whatever the source, through a single command-processing path. Commands run in file order, blank lines are ignored, and a nonexistent or unreadable file is handled in a controlled way. Because of the order in which this batch processed the milestones, this behavior was delivered earlier (execution control and Delivery 1 integration) and is specified in `application-execution` ("Interactive mode", "File mode", "Blank lines are ignored", "Supported execution forms") and `execution-control` ("Same command behavior for every input source"). The milestone's own example uses USER commands, which only now exist, so it has never been run as written.

## What Changes

- No new behavior and no requirement changes (`skip_specs: true`). The existing requirements already cover the milestone.
- Add a regression test that runs the milestone's example session (two USER CREATE, a USER UPDATE, EXIT, with a blank line) both from standard input and from a command file. It checks that the outputs are identical and in file order, and that a nonexistent file is reported in a controlled way.
- Re-verify with the packaged JAR in both modes.

### In scope
- Verification of F1-C07 with its own example.

### Out of scope
- Any change to input handling.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- None. The milestone is covered by `application-execution` and `execution-control`.

## Impact

- New test `CommandSequencesTest`. No production code changes.
