# Proposal

## Why

Milestone F3-C10 closes Delivery 3: commands from all three deliveries must coexist, `HELP` must list the Delivery 3 syntax (with every listed form on its own line), `HELP EXTENDED` must explain every command, and the technical restrictions (layers, Builder, Singleton controllers, injected non-singleton repositories, BCrypt dependency, generic attractions, pass consumption records, atomic group SpeedPass entry, `--fly`) must hold for an externally supplied command file.

## What Changes

- `HELP` shows each syntax form as its own line (the `;SPEED` entry variant and the GROUP invoice become separate lines) in the assignment's order: ATTRACTION commands, then SPEEDPASS/SPECIALPASS, INVOICE, HELP, HELP EXTENDED, EXIT; Delivery 1 and 2 lines unchanged and in their order.
- Add Delivery 3 restriction checks and an end-to-end scenario combining the three deliveries, executed from a command file normally and with `--fly`.
- No command semantics change.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: adds the Delivery 3 HELP content requirement.
- `project-initialization`: adds the Delivery 3 technical restrictions and end-to-end execution.

## Impact

- `CommandProcessor` (alternative syntax lines, registration order); new integration/compliance tests.
