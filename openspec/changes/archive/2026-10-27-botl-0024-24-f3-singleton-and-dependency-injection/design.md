# Design

## Context

`ParkController` (singleton) creates repositories in `start(...)`; other controllers are `new`-ed by `CommandProcessor` with the park controller as their only dependency and reach repositories through it.

## Goals / Non-Goals

**Goals:** explicit Singletons for controllers; repositories and collaborators injected at initialization; one composition root.
**Non-Goals:** repository interfaces, DI frameworks, behaviour changes.

## Decisions

- **Singleton form**: private constructor, lazily created static instance, `getInstance()`.
- **Injection by `configure(...)` method**: Singletons cannot receive constructor arguments from the composition root, so each exposes `configure(dependencies…)`:
  - `ParkController.configure(users, groups, tickets, attractions)` + `resetClock()` (date = today, price = 40.00).
  - `UserController.configure(UserRepository, ParkController)`
  - `GroupController.configure(GroupRepository, UserRepository, ParkController)`
  - `TicketController.configure(TicketRepository, UserRepository, GroupRepository, ParkController)`
  - `AttractionController.configure(AttractionRepository, TicketRepository, ParkController)`
  - `InvoiceController.configure(UserRepository, GroupRepository, TicketRepository, InvoiceBuilder)`
- **`ApplicationInitializer`** (business.controller, static methods): `startPermanent(base)`, `startTemporary(base)`, `startInMemory(UserRepository)` build the repositories (file-backed via `DataPaths`, or in memory), call every `configure`, reset the clock; `close()` deletes a temporary directory. `Main` calls it; `CommandProcessor` becomes parameterless and wires its handlers to the configured singletons.
- `ParkController` keeps park-wide queries (active tickets, locked groups, cleanup on user/group deletion) over its injected repositories.
