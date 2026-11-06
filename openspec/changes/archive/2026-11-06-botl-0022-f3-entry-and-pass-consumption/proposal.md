# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` with optional SpeedPass use (`;SPEED`) and automatic Special Pass consumption at SPECIAL attractions, including atomic group entry with SpeedPasses. Today passes can be bought and refunded, and SPECIAL attractions check for an unconsumed Special Pass, but no pass is ever consumed and `SPEED` is not accepted.

## What Changes

- Accept `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` at GENERAL, INDIVIDUAL and GROUP attractions. Every individual ticket involved (the ticket itself, or each composed ticket of a group ticket) must have an unconsumed SpeedPass. On success, exactly one SpeedPass per individual ticket is marked consumed and the output names the consumed passes.
- Make entry plus pass consumption indivisible. All conditions (date validity, type acceptance, Special Pass or SpeedPasses, presence, capacity) are checked before any change, and if any fails no one enters and no pass changes.
- At SPECIAL attractions the normal syntax automatically consumes the ticket's unconsumed Special Pass on successful entry, and the output says so. `;SPEED` is rejected there, as are group tickets (already rejected by type).
- Persist the consumed status immediately after an entry that consumed passes.
- Reject any fourth entry argument other than `SPEED`, and list the SPEED variant in help.

### In scope
- SPEED option, pass consumption, atomicity, output, persistence of consumption, help.

### Out of scope
- Leaving attractions or refunds (unchanged; consumed passes are already non-refundable).

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: the Special Pass requirement now includes consumption and forbids SPEED. New requirements cover individual and group entry with SpeedPasses and the SPEED HELP line.

## Impact

- Model: `AccessTicket.individualTickets()`. `EntryRules` grows SpeedPass and consumption operations. `Attraction.admit` takes the SPEED option and returns an entry result with the consumed passes. `SpecialAttraction` consumes its pass and disallows SPEED.
- `ParkController.enterAttraction(attraction, ticket, speed)` implements `EntryRules` over its passes and saves after consumption.
- `CommandProcessor` (2 or 3 arguments, output) and catalog. Tests, UML, README.
