# Proposal

## Why

SpeedPasses and Special Passes can be bought but not used. Attraction entry must use them according to the attraction type, without ever consuming a pass or admitting part of a group when the entry fails.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps working as before at GENERAL, INDIVIDUAL and GROUP attractions.
- `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use: at GENERAL/INDIVIDUAL an individual ticket consumes exactly one of its unused SpeedPasses; at GENERAL/GROUP a group ticket requires an unused SpeedPass on every constituent ticket and consumes one per member, all-or-nothing.
- SPECIAL attractions admit individual tickets only, require the ticket's unused Special Pass and consume it automatically on successful entry; `SPEED` is rejected there.
- Every access condition (type, date, presence, capacity, passes) is checked before any change; consumed passes are persisted; the response names the consumed passes.
- HELP lists the SPEED variant.

## Capabilities

### New Capabilities
- `pass-consumption`: pass-aware entry rules, atomicity and reporting.

### Modified Capabilities
- `attraction-types`: SPECIAL entry now needs (and consumes) the Special Pass.
- `application-control`: HELP lists `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED`.

## Impact

- `Attraction` (preflight check, hooks for SPECIAL), `ParkController.enter`, `PassController.consume`, `CommandProcessor`, `CommandCatalog`, tests.
