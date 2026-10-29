# Proposal

## Why

Milestone F3-C10 closes Delivery 3: all previous features must work together and the commands of the three deliveries must coexist; HELP must show the Delivery 3 minimum syntax and HELP EXTENDED a description of every command. One integration gap remains from the batch order: the user invoice (F3-C02) could not show SpeedPasses and Special Passes because passes did not exist yet; with passes now in place, the two features must work together.

## What Changes

- User invoices include, as personal purchases, the SpeedPasses and the Special Pass associated with the user's individual tickets — both tickets bought directly and the user's tickets inside group tickets — each with its price, in sections that are omitted when empty, and included in the total. Group invoices remain without members' passes.
- Verification of the Delivery 3 HELP minimum set and of HELP EXTENDED descriptions.
- End-to-end Delivery 3 integration test (all commands of the three deliveries together, both execution modes) and packaged-JAR runs with `input_fase3.txt` normally and with `--fly`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `invoices`: user invoices include the user's SpeedPasses and Special Passes.
- `command-interface`: HELP shows the Delivery 3 minimum syntax.

## Impact

- `UserInvoiceBuilder` (new passes step), `InvoiceDirector`, `InvoiceController`; tests.

## Ambiguities and unmet prerequisites (flagged)

- **USER commands** (Delivery 1 milestones F1-C02…F1-C07) are applied after this proposal in the batch order, so USER CREATE/DELETE/UPDATE are still not available; the end-to-end checks use programmatically registered users and command files therefore produce controlled errors for user-dependent commands.
- **Git history:** the F3 messages are recorded verbatim in the body of the batch commits, in F3-C01…F3-C10 order; Delivery 1 milestones follow them because of the batch order.
