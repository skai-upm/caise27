# Proposal

## Why

Milestone F2-C06 lets users access attractions with their individual tickets through `ATTRACTION ENTER`, checking that the ticket exists, is valid for the current system date, and that the attraction has room.

## What Changes

- `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket: the ticket must exist and be dated on the current system date; the attraction must exist and have a free place; the ticket's user enters the attraction.
- The date is checked only on entry; presence inside does not depend on later date changes.
- The access operation works on the common ticket abstraction (the users a ticket admits), so group tickets can plug into the same mechanism in F2-C07; until then, entering with a group ticket is rejected.
- HELP lists `ATTRACTION ENTER`.
- Record the mandatory milestone commit `[F2-C06] Add individual attraction access` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-management`: adds entering an attraction with an individual ticket.
- `command-processing`: HELP lists `ATTRACTION ENTER`.

## Impact

- `Ticket.getHolders()` (abstract users-admitted contract) implemented by `IndividualTicket`; `AttractionService.enter`; `CommandProcessor` dispatch and HELP.
