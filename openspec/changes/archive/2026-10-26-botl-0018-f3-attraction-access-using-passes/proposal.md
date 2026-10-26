# Proposal

## Why

Passes can be bought but have no effect yet. Attraction entry must use them: SpeedPasses on request at the attractions that allow them, and the Special Pass automatically at SPECIAL attractions, keeping entries atomic and consuming passes only when an entry succeeds.

## What Changes

- `ATTRACTION ENTER;<atraccion>;<entrada>` keeps normal entry; `ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` requests SpeedPass use.
- GENERAL and INDIVIDUAL attractions: an individual ticket with `SPEED` consumes exactly one unused SpeedPass of that ticket.
- GENERAL and GROUP attractions: a group ticket with `SPEED` requires at least one unused SpeedPass on every individual ticket of its composition and consumes one per member; if any member lacks one or any other condition fails, nobody enters and nothing is consumed.
- SPECIAL attractions: individual entries only; the system finds the ticket's Special Pass, checks it is unused and consumes it only after a successful entry; `SPEED` is rejected there.
- The entry response reports which SpeedPasses or Special Pass were consumed; consumption is persisted.
- HELP/HELP EXTENDED list the SPEED option.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `attractions`: SPEED entry option, pass-based access rules per type, SPECIAL requires the Special Pass.
- `passes`: passes are consumed by attraction entry and the consumption is persisted.
- `uml-model`: the attraction controller's collaboration with the pass controller is shown.

## Impact

- `Attraction` entry becomes a template that validates passes and capacity before mutating; `SpecialAttraction` overrides the pass step; access interfaces expose their individual tickets. `AttractionController` (now configured with `PassController`), `CommandProcessor`, `CommandCatalog`, `App`, UML and tests updated.
