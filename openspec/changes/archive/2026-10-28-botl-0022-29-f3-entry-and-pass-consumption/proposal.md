# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` with optional SpeedPass use (`ATTRACTION ENTER;<attraction>;<ticket>;SPEED`) and automatic Special Pass consumption at SPECIAL attractions, with group entry using SpeedPasses as an indivisible operation. Passes exist since F3-C06 and attraction types since F3-C07, but entering does not consume anything yet.

## What Changes

- Accept the optional fourth field `SPEED` in `ATTRACTION ENTER` (any other value is rejected).
- Individual entry with `SPEED`: the ticket needs at least one unconsumed SpeedPass; on success exactly one is consumed and named in the output; on any failure no pass changes.
- Group entry with `SPEED` (GENERAL and GROUP attractions): every individual ticket of the composition needs an unconsumed SpeedPass; date, type, capacity and passes are all checked before any change; on success all members enter and exactly one SpeedPass per member ticket is consumed; otherwise nothing changes.
- SPECIAL attractions: normal syntax only; the ticket's Special Pass is found automatically, must be unconsumed, and is consumed only when entry succeeds (output says so); `SPEED` and group tickets are rejected.
- All earlier entry checks (attraction and ticket exist, ticket valid today, capacity, type) still apply; consumed state persists through the existing pass storage.
- Extend HELP/HELP EXTENDED.
- Commit with the exact message `[F3-C08] Extend attraction access and pass consumption`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attraction-access`: SpeedPass entry, atomic group SpeedPass entry, Special Pass consumption, SPEED not allowed at SPECIAL attractions.
- `execution-control`: HELP lists the `;SPEED` variant.

## Impact

- `Attraction` entry hook, `EntryResult`, controller entry signature, `ATTRACTION ENTER` command (2–3 fields) and output, catalogue, tests.
- **Flagged ambiguities:** which SpeedPass is consumed when several are available is free; the first unconsumed one in purchase order is used. Output adds one line per consumed pass: `SPEEDPASS CONSUMED: <id>` or `SPECIALPASS CONSUMED: <id>`.
