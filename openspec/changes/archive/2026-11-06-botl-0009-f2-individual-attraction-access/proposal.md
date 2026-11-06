# Proposal

## Why

Milestone F2-C06 lets users use their individual tickets to get into attractions. Attractions and individual tickets exist, but nobody can enter an attraction, so occupants and capacity have no effect yet.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`. For an individual ticket, its associated user enters the attraction when the ticket exists, is valid for the current system date (dated exactly that day), the user is not already inside, and the attraction has free capacity. Otherwise nothing changes and an error is displayed.
- The ticket date is checked only at entry. Later changes to the system date do not remove users already inside.
- Access is built on the common ticket abstraction: every ticket type states which users it admits, and a single access operation checks validity and capacity for all of them. It does not depend on the concrete ticket type.
- List the command in HELP.

### In scope
- Entry with individual tickets, its validity and capacity rules, and the type-independent access operation.

### Out of scope
- The group-ticket access rules (all members enter together, indivisible capacity check), which are specified and verified by F2-C07. Leaving an attraction (not defined by the source).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: adds entry with an individual ticket (ADDED requirements) and the HELP entry for ATTRACTION ENTER.

## Impact

- `Ticket` gains an abstract "users admitted" operation, implemented by `IndividualTicket` and `GroupTicket`.
- `ParkController.enterAttraction`. `CommandProcessor` gets the ATTRACTION ENTER handler and HELP line. New tests.
