# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` with optional SpeedPass use (`;SPEED`) and automatic Special Pass consumption at SPECIAL attractions, including atomic group entry with one SpeedPass per member, without modifying any pass when entry fails.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`: with an individual ticket, at least one unconsumed SpeedPass of the ticket is required and exactly one is consumed on success; with a group ticket (GENERAL and GROUP attractions), every composed individual ticket needs an unconsumed SpeedPass and exactly one per ticket is consumed. The output names each consumed pass.
- At SPECIAL attractions, normal entry finds the ticket's Special Pass automatically and marks it consumed only when entry succeeds; the output says so. `;SPEED` is rejected at SPECIAL attractions.
- All previous checks (attraction and ticket exist, ticket valid today, accepted ticket type, capacity, nobody already inside) plus the pass checks run before any state change; on any failure nobody enters and no pass changes.
- HELP / HELP EXTENDED list the `;SPEED` variant.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: SpeedPass entry (individual and group), Special Pass consumption, atomicity of entry and consumption.
- `command-interface`: HELP lists the `;SPEED` variant.

## Impact

- `Attraction` entry template and `SpecialAttraction`, `PassLookup`, `AccessTicket`, `ParkController.enterAttraction`, `CommandProcessor`, catalog, tests, UML.

## Ambiguities (flagged)

- When several SpeedPasses are available, the oldest unconsumed one (purchase order) is consumed.
- The fourth field must be exactly `SPEED`; any other value is rejected as an invalid command.
- Output: one `USER <email> ENTERED ATTRACTION <id>` line per user, then `SPEEDPASS <id> CONSUMED` / `SPECIALPASS <id> CONSUMED` for each consumed pass.
