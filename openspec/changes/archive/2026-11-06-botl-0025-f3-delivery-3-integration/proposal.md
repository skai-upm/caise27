# Proposal

## Why

Milestone F3-C10 closes Delivery 3. All features must work together, commands from the three deliveries must coexist, and HELP must show the Delivery 3 syntax (with purposes in HELP EXTENDED). The Delivery 3 technical restrictions must hold, and the JAR must process an instructor command file normally and with `--fly`, leaving no data after `--fly`. Each Delivery 3 feature was verified on its own, but no single session yet exercises them together with the Delivery 2 features through the packaged entry point. HELP's mandated list in the specs still stops at Delivery 2.

## What Changes

- Extend the HELP requirement with the mandated Delivery 3 syntax lines, and verify them verbatim (the implementation already prints them).
- Add an end-to-end Delivery 3 integration test (normal run with restart, and `--fly` run) combining system settings, groups, individual and group tickets, typed attractions, SPEED entries, Special Pass consumption, refunds, cascades, invoices, HELP and HELP EXTENDED, and EXIT.
- Review the Delivery 3 technical restrictions against the code and record the evidence: layers, Builder, singleton controllers, configurable non-singleton repositories, dependency injection, BCrypt via Maven, bounded generics, consumed passes kept, and atomic group SpeedPass entry.
- Verify the packaged JAR with an `input_fase3.txt` file in both modes.
- Document a Delivery 3 example session in the README.

### In scope
- HELP requirement, integration test, restriction review, JAR verification, README.

### Out of scope
- The Delivery 1 USER commands, which the batch processes after this milestone (validation, registration, persistence, deletion, update). HELP lists their syntax as mandated, but they cannot be executed yet. The integration test registers users through the configuration.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: the HELP requirement includes the Delivery 3 syntax list.

## Impact

- New `Delivery3IntegrationTest`. `CommandProcessorTest` checks the Delivery 3 lines. README.
- No production code change is expected. Any defect found by the integration test is fixed within this change.
