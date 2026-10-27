# Proposal

## Why

Milestone F3-C10 closes Delivery 3: all features of the three deliveries must work together, HELP must show the commands of all deliveries, and the Delivery 3 technical restrictions (layers, Builder, Singleton controllers with injected repositories, BCrypt, bounded generics, persistent consumed passes, atomic group SpeedPass entry) must hold in the delivered JAR, runnable with `input_fase3.txt` normally and in `--fly` mode.

## What Changes

- Verify HELP lists the complete Delivery 3 syntax together with the Delivery 1 and 2 commands, and HELP EXTENDED describes each.
- Add an end-to-end Delivery 3 session test (users, groups, tickets, group tickets, typed attractions, SpeedPasses and Special Passes, SPEED and special entries, invoices, refunds, persistence after restart) in normal and `--fly` modes, including rejected operations.
- Add automated checks for the Delivery 3 technical restrictions not yet covered.
- Verify a from-scratch build and both execution forms with `input_fase3.txt`, and the mandatory Delivery 3 commits.
- Record the mandatory milestone commit `[F3-C10] Complete delivery 3 integration` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: Delivery 3 commands coexist with Delivery 1 and 2 commands; HELP shows all of them.
- `project-build`: Delivery 3 technical restrictions.

## Impact

- New `Delivery3IntegrationTest`; additional assertions in existing architecture tests. No production behaviour change expected.
