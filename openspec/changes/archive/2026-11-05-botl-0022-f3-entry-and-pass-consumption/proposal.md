# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` with optional SpeedPass use (`;SPEED`), for individual tickets and atomically for whole group tickets, and with automatic Special Pass consumption at SPECIAL attractions. The pass model and attraction types already exist, but entry never consumes passes.

## What Changes

- Accept `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`. All previous checks still apply. The attraction type must allow SpeedPasses: GENERAL, INDIVIDUAL and GROUP do, SPECIAL does not.
- Individual SpeedPass entry consumes exactly one unconsumed SpeedPass of the ticket, and the output names it.
- Group SpeedPass entry requires an unconsumed SpeedPass for every component. All members enter and one pass per component is consumed, or nothing changes.
- SPECIAL entry consumes the ticket's unconsumed Special Pass only on success, and the output names it (**MODIFIED** requirement).
- Every check runs before any state change, so failed entries never alter occupants or passes.
- `HELP` / `HELP EXTENDED` list the `SPEED` variant.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attractions`: SpeedPass entry (individual and group), and Special Pass consumption on successful special entry.

## Impact

- `GeneralAccess.individualTicketIds()`.
- New `Attraction.admit` / `Admission`, with `acceptsSpeedPasses` and `requiredPasses` hooks.
- `ParkController.enterAttraction(..., speed)`.
- `AttractionCommands`, catalog and tests.
