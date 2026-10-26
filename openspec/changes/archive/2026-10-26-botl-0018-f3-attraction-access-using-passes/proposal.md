# Proposal

## Why

Visitors can buy SpeedPasses and Special Passes but cannot use SpeedPasses anywhere, and the use of passes is not reported nor durably recorded. Entry must optionally consume SpeedPasses, atomically for whole groups, and always consume the Special Pass at SPECIAL attractions, telling the operator what was consumed.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps the normal entry; `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use.
- SPEED with an individual ticket (GENERAL, INDIVIDUAL) consumes exactly one AVAILABLE SpeedPass of that ticket; with a group ticket (GENERAL, GROUP) one per internal ticket. If any member lacks one or any other condition fails, nobody enters and nothing is consumed.
- SPECIAL attractions: individual entries only; the ticket's Special Pass is located automatically, must be AVAILABLE and is consumed only after a successful entry; `SPEED` is rejected there.
- The response reports the consumed passes; consumption is persisted (fixes botl-0017, where the Special Pass status changed only in memory).
- Unknown option (not `SPEED`) → error. HELP catalog and UML updated.
- Required commit title: `[F3-C08] Extend attraction access and pass consumption`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `attraction-management`: SPEED option, SpeedPass consumption, consumption reporting and persistence, SPECIAL rejects SPEED.

## Impact

- `Admission` gains `getIndividualAccesses()`, `IndividualAccess` gains `findAvailableSpeedPass()`; `Attraction.enter` gains the speed flag and returns an `EntryResult` (users entered, passes consumed); `SpecialAttraction` hooks adapted; `TicketManager` exposes a save after consumption; `ParkController`, `CommandProcessor`, `CommandCatalog` updated.
