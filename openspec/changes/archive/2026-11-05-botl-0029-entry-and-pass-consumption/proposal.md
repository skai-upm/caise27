# Proposal

## Why

Milestone F3-C08 connects passes with attraction access: `ATTRACTION ENTER` gains an optional `SPEED` variant that consumes one SpeedPass per individual ticket (atomically for group tickets), and SPECIAL attractions require and automatically consume the ticket's Special Pass. Entry and consumption must be indivisible.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` (in addition to the 3-element form). Previous checks still apply (existence, date validity, capacity, attraction type).
- Individual ticket with `SPEED` (GENERAL/INDIVIDUAL): needs an unconsumed SpeedPass; on success exactly one is consumed and named in the output.
- Group ticket with `SPEED` (GENERAL/GROUP): every composition ticket needs an unconsumed SpeedPass; on success all members enter and one SpeedPass per composition ticket is consumed; otherwise nothing changes.
- SPECIAL attraction: normal syntax; requires the ticket's unconsumed Special Pass, consumed (and reported) only on successful entry; `SPEED` is not permitted there.
- Any failure leaves occupants and passes unchanged.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attraction-access`: SpeedPass entry, Special Pass entry and their atomic consumption.

## Impact

- `Attraction.enter` gains a pass step (pass lookup interface in the model); `SpecialAttraction` rules; `TicketAccess.individualTickets()`; `AttractionService.enter` with the pass repository; `ATTRACTION ENTER` with an optional 4th element.
