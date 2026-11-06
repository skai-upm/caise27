# Proposal

## Why

Milestone F2-C06 (Delivery 2, step 6) lets individual tickets be used with `ATTRACTION ENTER` so their user gets into an attraction, provided the ticket is valid for the current system date and the attraction has room. Attractions exist (F2-C05) but nobody can enter them yet.

## What Changes

- `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket: the ticket must exist and be dated on the current system date (checked only at entry); the attraction must have free capacity for the user; the ticket's user then enters.
- Presence inside the attraction does not depend on later system-date changes.
- Access is built on the common ticket abstraction (each ticket type tells which users it admits), so group tickets can reuse it in F2-C07 without a separate mechanism.
- `HELP` lists `ATTRACTION ENTER`.

### In scope
- Source section 10 for individual tickets.

### Out of scope
- Entering with group tickets and indivisible group capacity (F2-C07); leaving attractions (not defined by the source).

## Capabilities

### New Capabilities
- `attraction-access`: entering attractions with tickets — ticket validity, capacity, admitted users.

### Modified Capabilities
- `execution-control`: `HELP` lists `ATTRACTION ENTER`.

## Impact

- `Ticket` gains `holders()` and `isValidOn(date)`; `AttractionService.enter`; `ParkController.enterAttraction`; `CommandProcessor` dispatch; tests.
