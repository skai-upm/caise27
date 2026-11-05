# Proposal

## Why

Milestone F2-C10 (Delivery 2, step 10) requires every Phase 1 and Phase 2 feature to work together. `HELP` must show at least the exact command syntax listed in source §15. The §16 technical restrictions must hold: object relationships with the specified directions, a common ticket abstraction with composition, a single central management instance, history retention, and `--fly` reusing the same persistence. The §20 submission flow must work: build with `mvn package` and run a command file both normally and with `--fly`, leaving no data behind. An audit of the current state found two gaps. `HELP` uses different placeholder names than §15 (for example `<firstName>`, `<id>`, `<ticket>`). The HELP requirements are also scattered across seven capability specs that each pin those placeholder spellings. Nothing exercises the full Delivery 2 flow end to end, and the README still describes only Delivery 1.

## What Changes

- Make `HELP` print exactly the §15 syntax lines, in that order, from `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>` to `HELP` and `EXIT`.
- Consolidate the HELP contract into the `execution-control` capability: its HELP requirement now lists the full §15 set. The seven per-capability "listed in HELP" requirements are removed in favor of it.
- Add a Delivery 2 integration test that runs a full Phase 1 + Phase 2 command file through the real entry point in normal mode (including a restart) and in `--fly` mode. It checks responses, restored state, and that `--fly` leaves no data.
- Add a guard test for the §16 relationship directions: `User` has no fields referencing tickets, groups or attractions, and `Group` has no ticket fields. It also checks the inheritance and composition of the ticket hierarchy.
- Update `README.md` with the Delivery 2 commands, the data files and `--fly`.

In scope: HELP alignment, integration verification, documentation.

Out of scope: new domain behavior.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `execution-control`: `HELP` must list the complete Delivery 2 command syntax of §15.
- `park-management`, `user-groups`, `tickets`, `group-tickets`, `attractions`: their individual "listed in HELP" requirements are removed, superseded by the consolidated `execution-control` requirement.

## Impact

- `CommandProcessor` help text; existing tests that assert old placeholder spellings; new integration and structure tests; `README.md`.
