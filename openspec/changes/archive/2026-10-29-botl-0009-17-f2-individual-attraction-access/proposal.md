# Proposal

## Why

Milestone F2-C06 lets individual tickets be used with `ATTRACTION ENTER` to access an attraction, checking the ticket's validity for the current system date and the attraction's capacity, through a mechanism built on the common ticket abstraction.

## What Changes

- `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`: the attraction and ticket must exist and the ticket must be valid on the current system date (its date equals the current date); the date is checked only at entry. With an individual ticket, its user enters the attraction.
- The attraction must have room for everyone who needs to enter; otherwise nobody enters.
- Access is computed through the common ticket abstraction (each ticket type reports the users it admits), so no separate access mechanism per ticket type is needed.
- Once inside, presence does not depend on later system-date changes.
- HELP lists `ATTRACTION ENTER`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: entering an attraction with an individual ticket.
- `command-interface`: HELP lists `ATTRACTION ENTER;<attraction>;<ticket>`.

## Impact

- `Ticket` gains an abstract "users admitted" operation implemented by `IndividualTicket` and `GroupTicket`; `ParkController.enterAttraction`; dispatch; tests.

## Ambiguities and unmet prerequisites (flagged)

- **Group tickets:** access with a group ticket (all composed users enter together, indivisibly) is milestone F2-C07. This change already routes access through the common abstraction but rejects group tickets with an explicit "not available yet" error, leaving the next milestone to enable them.
- **Composed individual tickets** (`TG01:<dni>`) belong to a group ticket; the source only shows top-level identifiers being used for entry. They are rejected for individual entry ("use the group ticket").
- **User already inside:** an attraction cannot hold the same user twice, so entering an attraction the user is already in is rejected. The source does not forbid being inside several attractions at once or using the same ticket several times on its day; neither is restricted here (ticket consumption is a later delivery).
- **Users** are still only registered programmatically (no USER CREATE yet), so through commands no individual ticket can be created and ATTRACTION ENTER reports the ticket as not found.
- Output text is not specified: `USER <email> ENTERED ATTRACTION <id>` per user.
