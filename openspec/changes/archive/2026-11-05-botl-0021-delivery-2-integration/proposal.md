# Proposal

## Why

Milestone F2-C10 closes Delivery 2: all Delivery 1 and Delivery 2 commands must coexist, `HELP` must show the full command syntax listed by the assignment, and the technical restrictions (Java 21/Maven, executable JAR in interactive/file/`--fly` modes, no databases/frameworks/external libraries, object relationships with the required directions, ticket abstraction, single park manager, historical tickets preserved) must hold. The current HELP uses different placeholder spellings and order from the assignment's list.

## What Changes

- `HELP` shows exactly the assignment's syntax lines, in its order: USER CREATE/DELETE/UPDATE, SYSTEM DATE/PRICE, GROUP CREATE/ADD/REMOVE/DELETE, TICKET CREATE/DELETE, GROUPTICKET CREATE/DELETE, ATTRACTION CREATE/ENTER/EXIT/LIST/STATUS, HELP, EXIT.
- Add Delivery 2 technical restriction checks (no Java object serialization; single park manager; relationship directions) to the compliance tests.
- Add an end-to-end Delivery 2 scenario executed from a command file in normal and `--fly` mode, verifying identical output and that `--fly` leaves no data behind.
- No change to command semantics.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: HELP must list the complete Delivery 2 syntax.
- `project-initialization`: adds the Delivery 2 technical restrictions and the end-to-end acceptance check.

## Impact

- `CommandProcessor` command registration order/usage strings; tests updated for the new placeholder spelling; new integration test.
