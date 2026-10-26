# Proposal

## Why

Passes can be bought but are never used. Attraction entry must now let visitors use SpeedPasses on request and Special Passes automatically, consuming them only when the entry actually succeeds and respecting the attraction type and the all-or-nothing rule for groups.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>` keeps normal entry; `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` requests SpeedPass use.
- SPEED with an individual ticket (GENERAL and INDIVIDUAL attractions) consumes exactly one unused SpeedPass of that ticket.
- SPEED with a group ticket (GENERAL and GROUP attractions) requires every individual ticket in the composition to have at least one unused SpeedPass and consumes one per member; if any member lacks one or any other condition fails, nobody enters and nothing is consumed.
- SPECIAL attractions admit individual tickets only, reject SPEED, and automatically use the ticket's Special Pass, which must exist and be unused; it is marked consumed only after the entry succeeds.
- Responses report which pass(es) were consumed.
- HELP lists the SPEED syntax.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attractions`: new requirements for SpeedPass entry (individual and group), Special Pass entry at SPECIAL attractions and the no-consumption-on-failure rule.
- `command-processing`: HELP lists `ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED`.

## Impact

- `Attraction` hierarchy (pass policy per type), admission interfaces expose the individual tickets they cover, `AttractionManager` (depends on `PassManager`), `PassManager.consumeAll`, `CommandProcessor`, `CommandRegistry`, `Bootstrap` initialisation order, tests and UML.
