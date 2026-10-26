# Proposal

## Why

Controllers are plain classes created with `new` in `App`, so nothing guarantees that each management responsibility has a single instance, and collaborations are wired ad hoc. Delivery 3 asks to formalise uniqueness with the Singleton pattern for conceptually unique controllers, while repositories stay ordinary objects created and configured at startup and injected wherever they are needed.

## What Changes

- Every conceptually unique controller — `MainController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`, `InvoiceController` — applies the Singleton pattern: private constructor and a single instance obtained through `getInstance()`.
- Repositories are **not** singletons: they are created at application startup with the paths of the chosen data directory and injected into the controllers that use them.
- Dependencies between controllers (for example `GroupController` → `UserController`, `TicketController` → `GroupController`, `AttractionController` → `TicketController`, `InvoiceController` → its collaborators) are established during application initialisation and never constructed inside the operations that use them.
- Normal and `--fly` modes only differ in the repository instances injected at startup; controller logic is unchanged.
- No command or output changes.

## Capabilities

### New Capabilities
- `component-configuration`: How application components are created and connected — singleton controllers, repositories created and injected at startup, controller-to-controller dependencies configured during initialisation, and mode selection purely through injected repositories.

### Modified Capabilities
<!-- None: commands and behaviour are unchanged. -->

## Impact

- All controllers get a private constructor, `getInstance()` and a `configure(...)` initialisation method receiving their repositories and collaborating controllers; `App` becomes the single place that creates repositories and configures controllers; `TestPark` and controller tests use the same initialisation path.
- `uml/model.puml` reflects singleton instances and injected dependencies. Tests for uniqueness, configuration and both modes.
