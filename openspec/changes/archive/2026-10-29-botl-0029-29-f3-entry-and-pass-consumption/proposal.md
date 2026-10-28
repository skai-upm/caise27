# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` so passes are actually used: an optional `SPEED` variant consumes SpeedPasses (one per individual ticket, atomically for groups), and special attractions automatically consume the ticket's Special Pass, all without changing any state when access fails.

## What Changes

- Accept `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` in addition to the Delivery 2 form; any other 4th element is rejected.
- Individual ticket with `SPEED` (GENERAL or INDIVIDUAL attractions): requires an unconsumed SpeedPass on the ticket; on success exactly one is consumed and reported.
- Group ticket with `SPEED` (GENERAL or GROUP attractions): every composition ticket must have an unconsumed SpeedPass; date, accepted type, capacity and passes are checked before any change; on success all members enter and exactly one SpeedPass per composition ticket is consumed; otherwise nothing changes.
- SPECIAL attractions: normal syntax only (`SPEED` rejected; group tickets rejected); the ticket's unconsumed Special Pass is found automatically and consumed only on successful entry; output reports it.
- Output lines for consumed passes: `SPEEDPASS CONSUMED: <id> (ticket <ticket>)` and `SPECIALPASS CONSUMED: <id> (ticket <ticket>)`, after the `USER ENTERED ATTRACTION` lines.
- `HELP`/`HELP EXTENDED` list the `SPEED` variant.
- Commit with the mandatory message `[F3-C08] Extend attraction access and pass consumption`.

## Capabilities

### New Capabilities
- `pass-consumption`: SpeedPass and Special Pass use on attraction entry, atomicity and reporting.

### Modified Capabilities
- `execution-control`: help lists `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

## Impact

- `Attraction` entry gains the SpeedPass option and a pass-consumption result (`EntryResult`); `SpecialAttraction` contributes its Special Pass; service, controller and processor pass the option; tests.
- Ambiguities flagged:
  - When several SpeedPasses are available, the first bought (purchase order) is consumed.
  - Consumed state is kept in memory; persisting passes and their state is F3-C09.
