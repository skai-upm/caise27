# Proposal

## Why

Milestone F3-C08 completes attraction access for Delivery 3: entry may optionally use SpeedPasses (`ATTRACTION ENTER;<attraction>;<ticket>;SPEED`), group entry with SpeedPasses must be atomic, and SPECIAL attractions must automatically require and consume the ticket's Special Pass.

## What Changes

- Add the optional `SPEED` variant of `ATTRACTION ENTER` at GENERAL, INDIVIDUAL and GROUP attractions:
  - individual ticket: needs at least one unconsumed SpeedPass; exactly one is consumed on success and named in the output;
  - group ticket: every individual ticket of the composition needs an unconsumed SpeedPass; all members enter and exactly one SpeedPass per component is consumed, or nobody enters and nothing is consumed.
- SPECIAL attractions: plain `ATTRACTION ENTER` automatically finds the ticket's Special Pass, requires it to be unconsumed and consumes it only on successful entry (stated in the output); the `SPEED` variant and group tickets are rejected.
- All previous checks (existence, date validity, accepted ticket type, capacity, not already inside) still apply; any failure leaves occupants and passes unchanged.
- HELP lists the SPEED variant.
- Commit with the exact message `[F3-C08] Extend attraction access and pass consumption`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attraction-access`: SpeedPass entry, Special Pass entry and atomic consumption.

## Impact

- `business.model`: `PassLookup`, `EntryResult`, `AttractionAccess.individualTickets()`, `Attraction.admit(..., speed, lookup)` with the `passesToConsume` hook overridden by `SpecialAttraction`; `AttractionController` receives the `PassRepository`; `AttractionCommands`; help catalogue.
- Consumed state is kept in memory here; persisting passes and their state is F3-C09.
- Ambiguities flagged: when several SpeedPasses are available the earliest purchased (first in store order) is consumed; outputs: `ENTERED A01: ana@correo.com (consumed SpeedPass SP01:1)` and `ENTERED A04: ana@correo.com (consumed Special Pass PE01)`; the last field must be exactly `SPEED`.
