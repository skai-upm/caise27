# Proposal

## Why

Milestone F3-C03 (Delivery 3, step 3) requires controllers with a unique management responsibility to apply Singleton explicitly, while repositories stay replaceable dependencies created at application initialization and injected — never global singletons — and controller collaborations configured during initialization. Today the park controller creates its repositories itself from a data directory and creates the entity services internally, so persistence configuration and business control are mixed.

## What Changes

- The entity controllers (`UserService`, `GroupService`, `TicketService`, `AttractionService`, `InvoiceService`) and the central `ParkController` apply Singleton: private constructor, single `getInstance()`.
- A repository configuration (`ParkRepositories`) is created during application initialization — loaded from the selected data directory (normal or `--fly`) or in memory — and injected into the controllers; repositories are ordinary instances, not singletons.
- `ParkController.start(repositories, today)` performs the injection and wires the collaborations between controllers (e.g. group locking uses the ticket controller's active-ticket query) once, at start-up; no operation creates dependencies.
- No dependency-injection framework: plain Java.

### In scope
- Source section 5.

### Out of scope
- New commands or behavior changes.

## Capabilities

### New Capabilities
- `dependency-configuration`: singleton controllers, injected replaceable repositories and initialization-time wiring of collaborations.

### Modified Capabilities
<!-- None: external behavior is unchanged. -->

## Impact

- Services get private constructors, `getInstance()` and `configure(...)`; `ParkController.start(ParkRepositories, LocalDate)` replaces `start(Path, LocalDate)`; new `persistence.repository.ParkRepositories`; `Main` creates the configuration; tests adapted; UML stereotypes.
