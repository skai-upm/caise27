# Proposal

## Why

Milestone F3-C10 closes Delivery 3: all features of the three deliveries must work together and their commands coexist; HELP must show at least the listed Delivery 3 syntax (with HELP EXTENDED describing every command); the Delivery 3 technical restrictions must hold; and `mvn package` must produce a JAR that runs a command file in normal and `--fly` mode without leaving `--fly` data behind.

## What Changes

- Add an end-to-end Delivery 3 integration test executing a command file that mixes Delivery 1, 2 and 3 commands (system date/price, groups, tickets, group tickets, typed attractions, SpeedPass and Special Pass purchase/refund, SPEED and special entries, exits, status, invoices, help, rejections) in normal mode with a restart check, and comparing normal and `--fly` runs with no leftover data.
- Add a test pinning the Delivery 3 minimum HELP lines and that HELP EXTENDED describes every command.
- Add a technical-restrictions test summarising the Delivery 3 design constraints (layers, Builder, singleton controllers, non-singleton injected repositories, BCrypt dependency, bounded generic attractions, consumed passes kept).
- Verify the packaged JAR with `input_fase3.txt` in both modes.
- Commit with the exact message `[F3-C10] Complete delivery 3 integration`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `application-execution`: Delivery 3 end-to-end execution and technical restrictions.

## Impact

- New tests only, unless integration reveals defects.
- **Flagged ambiguities / ordering conflicts:**
  - `USER CREATE`, `USER DELETE` and `USER UPDATE` (F1-C03, F1-C05, F1-C06) are applied after this milestone in this batch; they appear in HELP but cannot yet be executed. Integration scenarios register users through the persisted user file.
  - Source §18 requires the F3 commits in development order; the remaining F1 milestone commits will follow them in this batch (flagged since F1-C09).
