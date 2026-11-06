# Proposal

## Why

Milestone F3-C08 (Delivery 3, step 8) extends `ATTRACTION ENTER` with the optional `SPEED` variant (SpeedPass use for individual and group tickets, the latter atomically for every member) and the automatic consumption of the Special Pass at special attractions. Passes can be bought and refunded (F3-C06) and special attractions already require a Special Pass (F3-C07), but no pass is ever consumed.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` in addition to the existing form; any other option is rejected.
- Individual SPEED entry needs an unconsumed SpeedPass on the ticket and consumes exactly one, named in the output.
- Group SPEED entry (GENERAL and GROUP attractions) needs an unconsumed SpeedPass on every constituent ticket; entry of all members and consumption of one SpeedPass per constituent are a single indivisible operation.
- Entry to a SPECIAL attraction finds the ticket's unconsumed Special Pass automatically and consumes it only when entry succeeds, reporting it; `SPEED` is not allowed at SPECIAL attractions.
- All previous checks (attraction, ticket, validity, type, presence, capacity) still apply; on any failure neither occupants nor passes change.
- `HELP` shows the optional `SPEED`.

### In scope
- Source sections 12, 12.1, 12.2 and 12.3.

### Out of scope
- Persistence of passes and their consumed state (F3-C09).

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-access`: SpeedPass entry, Special Pass consumption and their atomicity.
- `execution-control`: `HELP` shows `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]`.

## Impact

- `Attraction` (`acceptsSpeedPasses`, `speedPassTickets`, `automaticPasses`), `SpecialAttraction`, `AttractionService` (entry result with consumed passes), `ParkController`, `CommandProcessor` (2–3 fields), catalog, README.
