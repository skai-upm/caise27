# Proposal

## Why

Passes can be bought but have no effect. Attraction entry must use them: SpeedPasses on request with the `SPEED` option and Special Passes automatically at SPECIAL attractions, consuming them only when the whole entry succeeds.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps working; new `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use.
- Individual entry with `SPEED` (GENERAL or INDIVIDUAL attraction) needs an unused SpeedPass of the ticket and consumes exactly one.
- Group entry with `SPEED` (GENERAL or GROUP attraction) needs an unused SpeedPass for every individual ticket of the group ticket and consumes one per ticket; all-or-nothing.
- SPECIAL attractions admit individual tickets only, automatically require the ticket's unused Special Pass and consume it on successful entry; `SPEED` is rejected there.
- If any condition fails (type, date, presence, capacity, passes), nobody enters and no pass is consumed. The output names the consumed passes.
- HELP lists the `SPEED` form.

The source suggests change ID `consume-passes-during-attraction-entry`; this batch uses the BOTL ID `botl-0018-18-f3-attraction-access-using-passes`. Mandatory milestone message: `[F3-C08] Extend attraction access and pass consumption`.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `attractions`: new requirements for SPEED entry, SPECIAL entry with Special Pass, and no consumption on failure.
- `application-control`: HELP lists `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED`.

## Impact

- `Attraction.allowsSpeed()`/`requiresSpecialPass()` (overridden by `SpecialAttraction`); `PassManager` availability lookup and persisted consumption; `ParkController.enterAttraction` orchestration; command parsing and catalog.
