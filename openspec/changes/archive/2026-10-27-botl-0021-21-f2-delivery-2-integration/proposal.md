# Proposal

## Why

Milestone F2-C10 closes Delivery 2: all features of both deliveries must work together, HELP must show the complete Delivery 2 command syntax, and the technical restrictions (object relationships with the required directions, ticket abstraction, single central controller, `--fly` reusing the same repositories) must hold in the delivered JAR.

## What Changes

- HELP shows at least the Delivery 2 syntax list from the source, including the Delivery 1 user commands with the Delivery 2 placeholders (`USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`).
- End-to-end tests of a complete Delivery 2 session (users, system date/price, groups, tickets, group tickets, attractions, access, exit, status, persistence) in normal and `--fly` modes, including rejected operations.
- Automated checks of the relationship directions required by the technical restrictions.
- Verify a from-scratch build and the four execution forms.
- Record the mandatory milestone commit `[F2-C10] Complete delivery 2 integration` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: the HELP requirement now uses the Delivery 2 syntax for the user commands.
- `project-build`: adds the Delivery 2 technical restrictions (relationship directions, ticket abstraction, single controller, no external libraries).

## Impact

- `CommandProcessor` HELP text and syntax error messages; new `Delivery2IntegrationTest` and `ModelStructureTest`; existing tests updated for the new HELP strings.
