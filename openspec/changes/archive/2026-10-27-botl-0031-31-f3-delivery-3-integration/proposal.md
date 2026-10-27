# Proposal

## Why

Milestone F3-C10 closes Delivery 3: commands of all three deliveries must coexist, HELP must show the full syntax (with HELP EXTENDED explaining each command), and the JAR must run an external `input_fase3.txt` normally and with `--fly` while respecting every Delivery 3 technical restriction.

## What Changes

- Verify that HELP lists every Delivery 1–3 command syntax required by the assignment and that HELP EXTENDED describes each of them.
- Add a Delivery 3 end-to-end test running a mixed command file (users, groups, tickets, group tickets, typed attractions, SpeedPass/Special Pass purchase, entry and consumption, refunds, invoices) normally and with `--fly`, comparing outputs and checking that `--fly` leaves no data and consumed passes remain stored.
- Extend the technical-restriction checks: BCrypt is the only non-JDK runtime dependency; passes are never deleted by use.
- Update `README.md` with the Delivery 3 commands, layers and run modes.
- Commit with the exact message `[F3-C10] Complete delivery 3 integration`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `delivery-integration`: Delivery 3 reference flow, help completeness and restrictions.

## Impact

- New `Delivery3IntegrationTest`; `TechnicalRestrictionsTest`; `README.md`. No production behaviour change is expected; any gap found by the integration test is fixed within this change.
