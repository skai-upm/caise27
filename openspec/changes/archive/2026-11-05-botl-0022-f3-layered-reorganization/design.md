# Design

## Context

Current packages:
- `command`: the processor and the parsing and rules for groups, tickets and attractions.
- `model`: the domain classes.
- `park`: `ParkController` and `SystemValues`.
- `repository`: the four repositories.
- `persistence`: the file stores and `StorageLocation`.
- `App`, at the root.

Rules and messages are produced inside the command classes as `"ERROR: …"` strings. `App` creates `UserFileStore`, `ParkFileStore` and `StorageLocation` itself.

## Goals / Non-Goals

**Goals:**
- Three layers with one-way dependencies (presentation → business → persistence), with the model in business.
- Exceptions as the error-flow mechanism, with identical observable messages.

**Non-Goals:**
- Changing command syntax or output, or introducing frameworks.

## Decisions

- **Package map**:
  - `presentation`: `App`, `CommandProcessor`, `UserCommands`, `GroupCommands`, `TicketCommands`, `AttractionCommands`, `InvalidCommandException`.
  - `business`: `ParkController`, `SystemValues`, `ParkSession`, and the managers `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`. The exceptions are `ParkException`, `NotFoundException`, `InvalidDataException` and `RuleViolationException`.
  - `business.model`: `User`, `UserField`, `UserValidator`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`.
  - `persistence`: the four repositories, `UserFileStore`, `ParkFileStore` and `StorageLocation`.

  The model lives in business because its rules (for example validation) are business rules. Persistence depends on `business.model` to store domain objects, which is the usual layered compromise; it never depends on managers or presentation.
- **Checked exceptions**. `ParkException extends Exception`, so every business operation declares what can go wrong and presentation must handle it. The subclasses distinguish the kind of failure without changing the printed message. `InvalidCommandException`, the presentation's own exception, covers wrong element counts and unparsable syntax.
- **Managers** take the `ParkController` (state holder) and implement the operations previously coded in the command classes: user create/delete/update, group create/add/remove/delete, ticket and group-ticket create/delete, attraction create/enter/exit, and system date/price. They return domain results (the created ticket, the entrants, and so on). Presentation builds the `OK` text. The messages of thrown exceptions are exactly the former error texts.
- **`CommandProcessor.process`** wraps dispatch in `try { … } catch (InvalidCommandException | ParkException e) { return "ERROR: " + e.getMessage(); } catch (RuntimeException e) { return "ERROR: the command could not be processed"; }`. Persisting after `OK` moves to the business side: managers call `park.persist()` after a successful mutation, which avoids string-prefix checks.
- **`ParkSession` (business)** opens storage from the program arguments: it uses `StorageLocation` and the stores, starts the execution, and exposes ignored-line counts and the remaining command arguments. It also closes storage (fly cleanup). `App` uses only `ParkSession`, so presentation no longer imports persistence.
- **Moves via `git mv`** so file history is preserved. Tests move to mirror their classes. `UmlModelConsistencyTest` and `uml/model.puml` cover the new packages and classes.

## Risks / Trade-offs

- [A large mechanical move can hide behavior changes] → The unchanged message texts and the full existing suite (including both delivery integration tests), plus a JAR run of the Delivery 2 command file compared against the pre-change output, detect regressions.
