# Proposal

## Why

Milestone F2-C06 lets individual tickets be used with `ATTRACTION ENTER` to get into an attraction. Until now attractions can be created and listed, but nobody can enter them.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`. The attraction and ticket must exist, and the ticket must be valid on the current system date (its date of use equals the system date). The attraction must have enough free capacity. When those hold, the user associated with an individual ticket is admitted.
- The date is checked only on entry. Presence afterwards doesn't depend on later system-date changes.
- Access goes through the common ticket abstraction ("which users does this ticket admit?") and one capacity check that admits everyone or no one. There is no ticket-type-specific access mechanism.
- If a user the ticket would admit is already inside that attraction, the entry is rejected and nothing changes.
- `HELP` lists `ATTRACTION ENTER`.

### Out of scope
- Group-ticket access semantics, which are specified in the next milestone (it reuses this mechanism), leaving attractions, and pass consumption.

### Assumptions
- "Valid for the current system date" means the ticket's date equals the current system date, since a ticket "will be valid only on the date specified at creation".
- How many times a ticket may be used is not stated, so reuse isn't restricted here.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: adds attraction entry using an individual ticket.

## Impact

- `Ticket` gains `holders()`.
- `ParkController.enterAttraction` and the `ATTRACTION ENTER` command.
- `HELP` and tests.
