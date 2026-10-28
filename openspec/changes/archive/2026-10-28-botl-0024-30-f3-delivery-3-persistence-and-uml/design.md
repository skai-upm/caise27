# Design

## Context

Attraction types and passes are already stored by `TextAttractionRepository`/`TextPassRepository` (F3-C06/F3-C07) and `--fly` uses `TextRepositories.inDirectory` with another directory. `uml/model.puml` covers the model package and `ParkController`; `UmlModelTest` checks only the model package.

## Decisions

- **Integration test** `Delivery3PersistenceIntegrationTest` through `App.run` in a temporary working directory: seeds `data/users.txt`, runs a Delivery 3 command file (typed attractions, tickets, SpeedPasses, Special Pass, SPEED and special entries, a cascading `TICKET DELETE`), restarts and checks state; runs a user-free Delivery 3 file in normal and `--fly` mode comparing outputs and leftovers.
- **Diagram packages**: `business.model`, `business.controller` (`<<singleton>>` stereotypes), `business.invoice`, `business.repository` (interfaces + `ParkRepositories` record), `business.security`, `persistence` (Text* implementations, `TextRepositories` factory), and the root composition elements (`App`, `presentation.CommandProcessor`) with `..>` dependencies labelled `configure`/`creates`.
- **Pass cardinalities**: `SpeedPass "0..*" --> "1" IndividualTicket : ticket` and `SpecialPass "0..1" --> "1" IndividualTicket : ticket` (the field lives in `Pass`; a note says so).
- **`UmlModelTest`**: checks declarations for every type found in the listed source packages (`interface`, `enum`, `abstract class`, `class`), fields only for the model package, and that every declared type exists among all main source files.
