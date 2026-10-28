# Proposal

## Why

Milestone F2-C06: users must be able to access attractions with their individual tickets via `ATTRACTION ENTER`, the ticket being valid only on the current system date and the attraction having room.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` for individual tickets: the attraction and ticket must exist, the ticket's date must equal the current system date (checked only at entry), the attraction must have room for everyone entering, and the ticket's user enters the attraction.
- Build entry on the common ticket abstraction (the users entering are those of the ticket's individual tickets) with an all-or-nothing capacity check, so group tickets (F2-C07) reuse the same mechanism. In this milestone group-ticket identifiers are rejected.
- Presence inside an attraction does not depend on later changes of the system date.
- `HELP` lists `ATTRACTION ENTER`.
- Commit with the mandatory message `[F2-C06] Add individual attraction access`.

## Capabilities

### New Capabilities
- `attraction-access`: Entering attractions with tickets — ticket validity on the current date, capacity, and the effect on occupants.

### Modified Capabilities
- `execution-control`: `HELP` also lists `ATTRACTION ENTER;<attraction>;<ticket>`.

## Impact

- `AttractionService.enter`, `ParkController.enterAttraction`, processor command; tests.
- Ambiguities flagged:
  - Entering an attraction the user is already inside is rejected (the occupants are a collection of users; a duplicate presence is meaningless). Being inside a different attraction is not restricted by the statement and is not checked.
  - Ticket reuse on the same day is not restricted in this delivery (consumption rules are not defined here).
  - Individual tickets that belong to a group ticket composition are individual tickets and may be used for their own user.
  - Response: `USER ENTERED ATTRACTION: <email>`.
