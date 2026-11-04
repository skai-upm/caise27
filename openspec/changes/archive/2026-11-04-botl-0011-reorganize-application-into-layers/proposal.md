# Proposal

## Why

All 19 production classes live in the single package `com.parque`, where command parsing and formatting, the main controller's rules, the domain model, services and file repositories sit side by side, and `CommandProcessor` mixes presentation (parsing/printing) with business control (system date, ticket price, cross-entity rules). Delivery 3 starts by physically separating presentation, business and persistence into distinct packages without changing any observable behaviour.

## What Changes

- Reorganise the code into layered packages (no class in the default package; none left directly in `com.parque` except the `App` bootstrap):
  - `com.parque.presentation` — command-based interaction: `CommandProcessor` (parses command text and formats responses) and new `CommandRunner` (reads commands from standard input or a command file and prints responses).
  - `com.parque.business.model` — domain model: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`.
  - `com.parque.business.service` — domain rules per entity: `UserValidator`, `UserService`, `GroupService`, `TicketService`, `AttractionService`.
  - `com.parque.business.controller` — new `ParkController`, the application's main controller: owns the system date and ticket price and applies the cross-entity rules previously embedded in `CommandProcessor` (ticket check before user deletion, group lock, ticket purchase with the current date/price, attraction entry/exit).
  - `com.parque.persistence` — repositories and file access: the five repositories and new `DataDirectory` (creation/deletion of the `--fly` temporary directory).
- `App` becomes the composition root: argument handling, choosing the data directory, wiring the layers, exit status.
- Tests move to the package of the code they test; a new architecture test enforces the allowed dependencies.
- **No** change to command syntax, responses, data file formats, or business rules; E1/E2 behaviour is preserved.

## Capabilities

### New Capabilities
- `application-layering`: The package structure, the responsibilities of each layer, and the allowed dependency directions.

### Modified Capabilities
- None (all existing behavioural requirements remain valid unchanged).

## Impact

- **Code**: every production and test file changes package/imports; `CommandProcessor` is split into `CommandProcessor` (presentation) + `ParkController` (business); `App` loop moved into `CommandRunner`; `--fly` directory helpers moved to `DataDirectory`.
- **Data/commands**: unchanged; verified by running the same command script before and after and comparing output and data files.
- **Docs**: `uml/model.puml` reorganised by packages, with the new classes.
- **Mandatory milestone message** from the source: `[F3-C01] Reorganize application into layered packages`.
- **Assumptions recorded**: `App` stays in `com.parque` as composition root (it is neither presentation logic nor business nor persistence, and it is not the default package); persistence depends on the business model (repositories read/write model objects) and business services depend on persistence repositories — no repository interfaces are introduced because the source does not ask for them.
