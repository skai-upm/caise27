# Proposal

## Why

Milestone F2-C10 (Delivery 2, step 10) closes Delivery 2: all features of both deliveries must work together, `HELP` must show every command, and the technical restrictions of section 16 must hold. One restriction is not yet met: groups keep their members as DNI strings, while section 16 requires in-memory domain relationships to be object references, not identifiers.

## What Changes

- Groups hold references to their member `User` objects (DNIs only appear in the text file); group persistence resolves members by DNI at load time.
- Update `uml/model.puml` and its consistency check: `Group --> User : members` is now a direct object association (the by-identifier exception is removed).
- Verify the complete Delivery 2 flow end-to-end: a Delivery 2 command file run normally and with `--fly` (identical output, no data left after `--fly`), with Delivery 1 commands still working.
- Document Delivery 2 commands and `--fly` in the README.

### In scope
- Source sections 15, 16 and 20.

### Out of scope
- Delivery 3 features. Git-history requirements (section 18) are met by the project history.

## Capabilities

### New Capabilities
- `delivery-2-integration`: end-to-end Delivery 2 behavior and its technical restrictions verified as a whole.

### Modified Capabilities
- `uml-model`: associations must be realized by object fields (no identifier-based associations).

## Impact

- `Group`, `GroupService`, `TicketService`, `GroupFileStore`, `GroupRepository`, `ParkController.start`; tests adapted; `uml/model.puml`, `UmlModelTest`, README.
