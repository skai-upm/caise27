# Design

## Context

`ParkController` (singleton) holds all state and business rules; `persistence.ParkStorage` builds four text repositories and copies state in/out of the controller; `App` creates `ParkStorage` and a `CommandProcessor` whose commands call `ParkController.getInstance()` themselves. Invoices are created by `ParkController`. Architecture rule: business must not import persistence/presentation.

## Goals / Non-Goals

**Goals:** repository ports + injection, controller-owned load/save, `InvoiceController` singleton with injected collaborator, processor/commands receiving controllers.

**Non-Goals:** changing behaviour, file formats or commands.

## Decisions

- **Ports** (`business.repository`): `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository` interfaces (same `load`/`save`/`getProblems` contracts as today) and a `ParkRepositories` record bundling them.
- **Adapters** (`persistence`): `TextUserRepository`, `TextGroupRepository`, `TextTicketRepository`, `TextAttractionRepository` (renamed classes implementing the ports) and `TextRepositories.inDirectory(Path)` factory returning a new `ParkRepositories` (fresh instances per call — no global state). `ParkStorage` is removed.
- **Park controller injection**: `configure(ParkRepositories)` stores the dependency; `loadState()` (returns problem messages; the user-resolution logic formerly in `ParkStorage`) and `saveState()` (no-op when unconfigured). Setter injection is used because the controller is a singleton.
- **`InvoiceController`** (`business.controller`, singleton): `configure(ParkController)`; `createUserInvoice`, `createGroupInvoice` moved from `ParkController` (which keeps read accessors). Using it unconfigured raises `OperationNotAllowedException`.
- **Presentation injection**: `CommandProcessor(ParkController, InvoiceController)` constructs the commands passing the controller each needs; `ControllerCommand` receives its `ParkController` by constructor and `InvoiceCreateCommand` its `InvoiceController`. The no-arg `CommandProcessor()` delegates to the singletons for convenience.
- **Composition root** `App.execute`: build repositories for the chosen directory → `park.configure(repos)` → `invoices.configure(park)` → `park.reset()` + `loadState()` → `new CommandProcessor(park, invoices)` with `saveState` after each command.
- **Test**: `DependencyInjectionTest` configures the controller with in-memory repository implementations and checks load/save round trip and singleton properties.
