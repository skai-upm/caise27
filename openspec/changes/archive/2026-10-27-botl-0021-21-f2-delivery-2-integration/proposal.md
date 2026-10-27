# Proposal

## Why

Milestone F2-C10 closes Delivery 2: Phase 1 and Phase 2 commands must coexist, HELP must show the full Delivery 2 command syntax listed in the assignment, and the delivery must run from an external command file both normally and with `--fly` while keeping every technical restriction.

## What Changes

- Update HELP to show exactly the Delivery 2 syntax list: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, the GROUP, TICKET, GROUPTICKET and ATTRACTION commands, `HELP` and `EXIT`.
- Add an end-to-end Delivery 2 integration test running a mixed Phase 1 + Phase 2 command file normally and with `--fly` (identical output; no data left by `--fly`).
- Extend the technical-restriction checks: users and groups hold no references to tickets or attractions; a single park controller instance.
- Update `README.md` with Delivery 2 commands and `--fly`.
- Commit with the exact message `[F2-C10] Complete delivery 2 integration`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `execution-control`: HELP lists the complete Delivery 2 syntax.
- `delivery-integration`: Delivery 2 reference flow and restrictions.

## Impact

- `CommandProcessor.HELP_TEXT`, tests referring to the former lowercase placeholders, new `Delivery2IntegrationTest`, `TechnicalRestrictionsTest`, `README.md`. No behaviour change besides HELP text.
