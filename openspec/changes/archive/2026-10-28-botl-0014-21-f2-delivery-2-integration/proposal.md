# Proposal

## Why

Milestone F2-C10 closes Delivery 2: all features must work together, commands of both deliveries must coexist, HELP must show at minimum the Delivery 2 syntax list, and the technical restrictions (relationship directions, common ticket abstraction, single park controller, historical tickets, `--fly` reusing the same repositories, executable JAR with a command file in normal and `--fly` mode) must hold.

## What Changes

- Update HELP to display exactly the Delivery 2 minimum syntax list (Phase 1 and Phase 2 commands with the placeholder names given in source §15).
- Add an end-to-end Delivery 2 integration test running a complete command file (system date/price, groups, individual and group tickets, attractions, entry/exit/list/status, deletions and rejected operations) through the application in normal mode and in `--fly` mode, and checking state across a restart.
- Add structural checks for the mandated relationship directions (users hold no ticket/attraction/group references; groups hold no ticket references; group tickets compose individual tickets; tickets share an abstraction; single controller instance).
- Commit with the exact message `[F2-C10] Complete delivery 2 integration`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-control`: HELP lists the Delivery 2 minimum syntax.
- `application-execution`: Delivery 2 technical restrictions on model relationships.

## Impact

- `HelpCommand` text, new integration and structure tests.
- **Flagged ambiguities / ordering conflicts:**
  - The `USER CREATE/DELETE/UPDATE` commands (F1-C03, F1-C05, F1-C06) come later in this batch; they appear in HELP as required but are not executable yet. Integration scenarios register users through the persisted user file of a previous execution.
  - Source §18 requires the F2 commits in development order; F1 milestone commits will follow after them in this batch (flagged since F1-C09).
