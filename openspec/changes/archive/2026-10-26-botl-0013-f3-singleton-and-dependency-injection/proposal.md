# Proposal

## Why

Each controller (users, groups, tickets, attractions, invoices) represents one management responsibility of the park and must exist only once, while repositories must stay replaceable (normal vs `--fly` paths, other implementations). Today uniqueness is only a convention of `App`, and the wiring is implicit in constructors.

## What Changes

- `UserController`, `GroupController`, `TicketController`, `AttractionController` and `InvoiceController` apply the Singleton pattern: private constructor and a single instance obtained with `getInstance()`.
- Their collaborators — repositories, other controllers, the system date and the invoice builder — are injected once during application initialization through a `configure(...)` method; no controller constructs its dependencies.
- Repositories are not singletons: `App` creates them at startup for the chosen data directory (normal or `--fly`) and injects them.
- Using a controller before it is configured fails with a clear error.
- No command or output changes. PlantUML updated.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `layered-architecture`: adds requirements for unique controllers and startup-time injection of repositories and controller dependencies.

## Impact

- All controller classes, `App.buildProcessor`, and every test that created controllers (now `getInstance().configure(...)`).
