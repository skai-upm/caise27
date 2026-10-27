# Proposal

## Why

Milestone F3-C03 requires the controllers that represent a unique management function to apply the Singleton pattern, while repositories remain replaceable dependencies created during application initialization and injected — with project code, no DI framework — into the controllers that need them, together with the collaborations between controllers. Normal and `--fly` persistence must differ only in the repository configuration.

## What Changes

- `UserService`, `GroupService`, `TicketService`, `AttractionService` and `InvoiceService` become singletons (private constructor, `getInstance()`), like `ParkController`.
- Controllers stop creating or looking up their dependencies inside operations; their repositories and the `ParkController` collaboration are injected once at initialization (`configure(...)`).
- Repository instances are created by a persistence factory (`ParkRepositories.fromDirectory(path)` / `inMemory()`) during initialization and are not singletons; `--fly` uses the same factory with a different path.
- `CommandProcessor` obtains its controllers at construction instead of creating new ones.
- Behaviour, commands and persisted formats are unchanged.
- Record the mandatory milestone commit `[F3-C03] Add singleton controllers and dependency injection` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `application-architecture`: adds singleton controllers and injected, replaceable repositories.

## Impact

- All services, `ParkController`, `CommandProcessor`, `App`; new `persistence.ParkRepositories`; tests updated to use the singletons and initialization; UML updated.
