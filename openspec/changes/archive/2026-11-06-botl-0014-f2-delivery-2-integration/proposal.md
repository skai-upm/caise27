# Proposal

## Why

Milestone F2-C10 closes Delivery 2. All features must work together, commands from both deliveries must coexist, HELP must show the exact Delivery 2 command syntax, and the technical restrictions of Delivery 2 must hold. The JAR must process an instructor command file both normally and with `--fly`, leaving no data after a `--fly` run. HELP currently uses placeholder spellings that differ from the mandated Delivery 2 syntax (for example `<firstName>` instead of `<FirstName>`, and `<AttractionIdentifier>` instead of `<attraction>`). There is no end-to-end check of a full Delivery 2 session.

## What Changes

- Align HELP with the exact Delivery 2 syntax list (USER, SYSTEM, GROUP, TICKET, GROUPTICKET and ATTRACTION commands, HELP, EXIT).
- Add an end-to-end Delivery 2 integration test. It runs one command file covering system settings, groups, individual and group tickets, attractions (create, enter with both ticket types, exit, list, status) and deletions through `App`. It checks a normal run plus restart (persistence) and a `--fly` run (no residue).
- Verify the Delivery 2 technical restrictions against the code: Java 21 and Maven, standard library only, no serialization or regex, ticket abstraction with composition, single central controller, historical tickets protected, and `--fly` reusing the same repositories.

### In scope
- HELP alignment, integration test, restriction verification, README.

### Out of scope
- The user model and USER commands (Delivery 1 milestones that the batch processes after this one). Consequently, the restriction that domain relationships use objects instead of strings cannot yet be met for users. Groups, tickets and attractions reference users by email until the user model exists. This is recorded as an open item for the user-model milestone, not solved here.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: the HELP requirement now mandates the full Delivery 2 syntax list.
- `attractions`: the three ATTRACTION HELP requirements use the Delivery 2 placeholder spelling.

## Impact

- `CommandProcessor.HELP_TEXT` and the usage messages of the affected handlers.
- Tests: HELP assertions updated and a new `Delivery2IntegrationTest`. README updated.
