## Why

Delivery 3, milestone F3-C08 (source section 12), extends `ATTRACTION ENTER` with:
- an optional `SPEED` variant, which uses SpeedPasses for individual and group tickets;
- automatic consumption of the Special Pass at special attractions.

Group entry with SpeedPasses must be atomic. A failed entry never modifies passes.

## What Changes

- **Command.** `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]`. Any other fourth element is rejected.
- **Model.** The generic `Attraction<T>` gains the pass rules, typed on its bound capability:
  - `acceptsSpeedPasses()`: false for SPECIAL.
  - `passesToConsume(T ticket, boolean speed, List<IndividualTicket> entering, PassLookup)`: it either returns the passes to consume on entry, or reports why entry is refused. With SPEED, that is one unconsumed SpeedPass per entering individual ticket; at SPECIAL, the unconsumed Special Pass.
  - `PassLookup` gains `passesOf(IndividualTicket)`.
- **Business.** `AttractionManager.enter(attraction, ticket, speed)` runs every check first:
  1. type and date;
  2. who enters;
  3. capacity;
  4. pass conditions.

  Only then does it consume the selected passes and admit the users, so a failure changes nothing. It returns an `Entry` result: the entrants plus the consumed passes.
- **Output.** The `OK` response names the consumed passes, for example `; SpeedPass consumed: SP02:1` or `; Special Pass consumed: PE01`.
- **Specs and help.** HELP and the catalog show the `[;SPEED]` option. The SPECIAL attraction requirement now consumes the Special Pass on success.
- **Recorded decision.** For a group with SPEED, SpeedPasses are required and consumed for the component tickets of the members who actually enter. Members already inside the attraction do not enter and use no pass. In the normal case (nobody inside) this is every component ticket, as the source states.

## Capabilities

### New Capabilities
- `pass-consumption`: the SPEED variant, individual and atomic group SpeedPass entry, and Special Pass consumption.

### Modified Capabilities
- `attraction-types`: the Special Pass is consumed on successful special entry.
- `execution-control`: HELP shows `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]`.

## Impact

- `business.model`: `Attraction`, `SpecialAttraction` and `PassLookup`.
- `persistence`: `PassRepository`.
- `business`: `AttractionManager` and a new `Entry` record.
- `presentation`: `AttractionCommands` and `CommandCatalog`.
- Tests, README and UML.
