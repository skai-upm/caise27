# Proposal

## Why

Milestone F2-C06 (Delivery 2, step 6) lets visitors use individual tickets with `ATTRACTION ENTER` to access an attraction (source §10). The ticket must exist and be valid for the current system date, checked only at entry. The attraction must have enough capacity, and access must be built on the common ticket abstraction rather than a mechanism per ticket type. Today attractions exist, but nobody can enter them.

## What Changes

- Add `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier`. It admits the ticket's user to the attraction when the attraction exists, the ticket exists, the ticket's date equals the current system date, and there is room for everyone who needs to enter. Otherwise nothing changes and an `ERROR` is printed.
- Presence inside an attraction is independent of later system-date changes.
- Add a common "holders" query to the ticket abstraction (the users who enter with a ticket), so the access operation needs no ticket-type-specific logic.
- Group tickets go through the same path in the next milestone ("access using group tickets"). Until then, `ATTRACTION ENTER` with a group ticket is rejected with an explicit "not available yet" error.
- A user already inside the attraction is not admitted twice and does not count against capacity.
- List the command in `HELP`.

In scope: individual-ticket access, capacity check, date check, the shared holders abstraction.

Out of scope: group-ticket access (next milestone), leaving attractions, and persistence.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: adds the `ATTRACTION ENTER` access requirements (ticket validity, capacity, admission) and their `HELP` listing.

## Impact

- `Ticket` gains `getHolders()` (implemented by `IndividualTicket` and `GroupTicket`); `AttractionCommands` gains `enter`; `CommandProcessor` dispatch and `HELP`; tests.
