# Proposal

## Why

Milestone F3-C10 (Delivery 3 integration) requires all features of the three deliveries to work together. `HELP` must show the full syntax list, including the Delivery 3 commands, and `HELP EXTENDED` must describe every command. The Delivery 3 technical restrictions must hold, and the JAR must run a phase-3 command file both normally and with `--fly`.

## What Changes

- `HELP` requirement updated to the complete Delivery 1–3 syntax list (**MODIFIED** `execution-control`). The catalog already contains every line, and a verbatim test locks it in.
- Add the requirement Delivery 3 technical restrictions (**ADDED** `application-runtime`).
- Add `Delivery3IntegrationTest`. It runs a full phase-3 flow through `App.registerCommands` with persistence: typed attractions, SpeedPass and Special Pass purchase, SPEED and special entry, group SPEED entry, refunds, invoices, help, and a restart. A second run processes `input_fase3.txt` with `--fly` and checks that no data remains.
- Add `Delivery3RestrictionsTest` (Builder hierarchy, singleton versus non-singleton repositories, generic bounds, consumed passes retained).

### Known gap (deferred, not silently resolved)
- `USER` commands, user registration and user persistence aren't implemented yet (they come from the remaining Delivery 1 milestones in this batch). User-dependent steps in the integration test use a test user lookup, and the e-mail-based user references stay until user registration introduces the registry.
- The mandated commit message is recorded in this change's commit.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `execution-control`: full Delivery 1–3 HELP list.
- `application-runtime`: Delivery 3 technical restrictions.

## Impact

- Tests only (`Delivery3IntegrationTest`, `Delivery3RestrictionsTest`, HELP assertion) plus specs. No production change is expected unless the integration test reveals a defect.
