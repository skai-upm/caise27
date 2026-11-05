## Why

Delivery 3, milestone F3-C09 (source sections 13 and 14), requires persistence to be extended so that it preserves and reconstructs the Delivery 3 elements:
- attraction types;
- SpeedPasses and Special Passes, with their ticket, stored price and consumed state.

The `--fly` mode must use the same repositories and mechanisms. The PlantUML model must also represent the final Delivery 3 design.

## What Changes

- **New `passes.txt` data file**, managed by `ParkFileStore`. One line per pass:

  ```
  S;<id>;<ticketId>;<price>;<consumed>
  P;<id>;<ticketId>;<price>;<consumed>
  ```

  `S` marks a SpeedPass and `P` a Special Pass; `<consumed>` is `true` or `false`.
  - Passes are loaded after the tickets, with the stored price, so a later `SYSTEM PRICE` never changes them.
  - A line is invalid, ignored and counted in the existing warning if any of these hold: unknown kind; empty or duplicate identifier; the ticket is not an existing individual ticket; invalid price; invalid state; a second Special Pass for the same ticket.
- **`ParkRepositories`** loads and saves passes together with groups, tickets and attractions. The "kept in memory" limitation of F3-C06 is removed.
- **Already in place, verified here:**
  - consumed passes stay while their ticket exists;
  - refunds and cascades remove passes from the saved state;
  - attraction types are persisted, which was done in F3-C07 under the section 10 compatibility rule;
  - occupants are never persisted.
- **`--fly`** writes `passes.txt` in its temporary directory through the same store. Only the location differs.
- **Model.** `SpeedPass` and `SpecialPass` gain constructors that restore a stored price, used only when loading.
- **UML.** The final Delivery 3 design is reviewed against the section 14 checklist. `ParkFileStore` gains its relationship to the pass repository. `UmlModelConsistencyTest` now requires the key Delivery 3 relationships, so the diagram cannot drift from them.
- **Housekeeping.** `.gitignore` gets `passes.txt`; README documents the file; `ParkSession`'s unreadable-data message lists `passes.txt`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `park-persistence`: passes are persisted with ticket, price and state, and fly mode includes them.
- `design-documentation`: the UML represents the final Delivery 3 design, and the required relationships are extended.

## Impact

- `persistence`: `ParkFileStore` and `ParkRepositories`.
- `business.model`: `SpeedPass` and `SpecialPass` (restore constructors).
- `business`: `ParkSession` message.
- `uml/model.puml`, `UmlModelConsistencyTest`, the persistence tests, README and `.gitignore`.
