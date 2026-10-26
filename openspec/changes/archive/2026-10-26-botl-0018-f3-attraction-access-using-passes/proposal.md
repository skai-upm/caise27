# Proposal

## Why

Passes can be bought but have no effect yet. Delivery 3 makes `ATTRACTION ENTER` use them: SpeedPasses on request at general, individual and group attractions, and the Special Pass automatically at special attractions — always respecting the attraction type, atomic group entry and the rule that a failed entry consumes nothing.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps normal entry (no pass used) at GENERAL, INDIVIDUAL and GROUP attractions.
- New form `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use:
  - individual ticket at GENERAL or INDIVIDUAL: consumes exactly one unused SpeedPass of that ticket;
  - group ticket at GENERAL or GROUP: every individual ticket of the composition must have an unused SpeedPass; one is consumed per member; if any member lacks one, or any other condition fails, nobody enters and nothing is consumed.
- SPECIAL attractions: individual entries only; the ticket's Special Pass is located automatically, must not be consumed, and is marked consumed only after a successful entry. `SPEED` is rejected at SPECIAL attractions.
- A failed entry never consumes passes. Consumed passes stay stored with their new status.
- The output reports which SpeedPasses or Special Pass were consumed.
- HELP lists the new syntax.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attraction-management`: entry gains the optional `SPEED` mode, Special Pass consumption at SPECIAL attractions, atomic pass consumption for groups, and reporting of consumed passes.

## Impact

- `Admission` exposes the individual tickets it is made of and `IndividualAdmission` its Special Pass; `Attraction.admit` takes the speed flag, selects the passes to consume before entering, and returns an `EntryResult` (users admitted, passes consumed); `SpecialAttraction` implements its pass rule; `AttractionController.enter` persists consumption; `CommandProcessor` registers the `SPEED` syntax and reports consumed passes.
- `uml/model.puml` updated. Tests for every attraction type × ticket kind × mode, atomicity and persistence of consumption.
