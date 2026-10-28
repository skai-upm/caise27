# Proposal

## Why

Milestone F3-C03 requires controllers with a unique management responsibility to apply Singleton, repositories to be replaceable dependencies created and configured at application initialization and injected where needed (not global singletons), and collaborations between controllers to be wired by injection at initialization — all with project code, no DI framework — so that normal and `--fly` modes differ only in repository configuration. Today the park controller does not use repositories at all: the entry point drives a storage helper that reads/writes controller state from outside, and invoice generation lives inside the park controller.

## What Changes

- Define repository abstractions (users, groups, tickets, attractions) in the business layer; the text-file classes in the persistence layer implement them.
- Inject the configured repositories into the park controller during initialization; the park controller loads and saves the park state through them. Repositories are plain instances (not singletons); a factory builds them for a given directory (data or `--fly` temporary directory).
- Extract invoice generation into an `InvoiceController` singleton whose collaboration with the park controller is injected at initialization.
- Inject the controllers into the command processor (constructor injection) and into the commands it registers, instead of commands looking controllers up themselves.
- Behaviour, output and file formats are unchanged.
- Commit with the exact message `[F3-C03] Add singleton controllers and dependency injection`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `application-architecture`: adds requirements for singleton controllers, injected replaceable repositories and injected controller collaborations.

## Impact

- New `business.repository` package and `InvoiceController`; persistence classes renamed to `Text*Repository` implementing the interfaces; `ParkStorage` replaced by controller load/save; `App` becomes the composition root wiring everything; command classes receive their controller; tests updated.
- **Flagged ambiguities:**
  - Singletons need injection after construction, so injection into singleton controllers is done through explicit `configure(...)` methods called once at initialization (constructor injection is used for non-singleton collaborators such as the command processor).
  - The no-argument `CommandProcessor` constructor is kept as a convenience that wires the singleton controllers, for tests and simple embeddings; `App` wires explicitly.
