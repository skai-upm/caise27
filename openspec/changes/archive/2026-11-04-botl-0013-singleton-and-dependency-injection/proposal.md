# Proposal

## Why

Controller objects (`ParkController`, `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`) each represent one management responsibility of the application. Today nothing prevents several instances from being created, and `ParkController` builds its `InvoiceDirector` inside each invoice operation. Delivery 3 requires these controllers to be explicit Singletons. Their repositories and collaborators must be created at startup and injected, keeping repositories replaceable so that normal and `--fly` runs differ only in configuration.

## What Changes

- The five controllers apply the Singleton pattern:
  - private constructor;
  - `initialize(dependencies...)` creates the single instance once per execution and fails if one already exists;
  - `getInstance()` gives global access and fails before initialization;
  - `reset()` ends the execution's instance.
- Repositories (`*FileStorage`) are not Singletons. `ParkSession` (the composition root) creates them at startup with the paths of the selected mode and injects them through `initialize(...)`.
- Controller collaborations are wired during initialization:
  - `GroupManager` receives `UserManager`;
  - `TicketManager` receives `UserManager` and `GroupManager`;
  - `ParkController` receives the four managers and an `InvoiceDirector`, which it no longer constructs inside the invoice operations.
- `ParkSession.close()` resets all controllers; a new session (execution) can then initialize them again.
- No behaviour, command or file change; no DI framework.

## Capabilities

### New Capabilities
- `component-wiring`: Singleton controllers, startup creation and injection of repositories and collaborators, and mode-independent controller logic.

### Modified Capabilities
_None._

## Impact

- `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `ParkController` (Singleton API), `ParkSession` (wiring, reset), tests (use `initialize`/`reset` instead of `new`), `uml/model.puml` (injected `director` association).
