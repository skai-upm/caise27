# Proposal

## Why

Passes can be bought but not used. Attraction entry must now honour SpeedPasses (on request) and Special Passes (automatically at SPECIAL attractions). Failed entries must never consume purchases or partially admit groups.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps normal entry. New `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use:
  - an individual ticket consumes one unused SpeedPass;
  - a group ticket consumes one per constituent ticket, all or nothing.
- At `SPECIAL` attractions, only individual tickets are admitted. The ticket's unused Special Pass is located and consumed automatically, and `SPEED` is rejected.
- The order of operations guarantees atomicity: preflight pass selection, then the entry checks and admission, then marking the passes consumed and persisting them. Occupancy is rolled back if persisting fails.
- Replies report the consumed passes. The existing date, capacity and ticket-kind checks are unchanged.
- HELP/catalog list the SPEED form.

## Capabilities

### New Capabilities
_None._

### Modified Capabilities
- `attraction-access`: "Entry with a ticket" (optional `SPEED`); added "SpeedPass entry", "SPECIAL attraction entry" and "Failed entries consume nothing".
- `application-control`: HELP lists `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED`.

## Impact

- `ParkController` (entry orchestration), `PassManager` (pass selection and atomic consumption), `Attraction` (rollback helper), `CommandProcessor`, `CommandCatalog`, tests, UML (new `EntryResult` record).
