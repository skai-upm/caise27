# Design

## Context

`ParkController` is a singleton. `ParkStorage(Path)` builds three repositories internally. `App.runWithStorage` calls `storage.load(park)` and `storage.save(park)` directly. `LayeringTest` forbids business→persistence imports. See proposal.md - Why.

## Decisions

- **Port in business**: `interface ParkRepository { List<String> load(ParkController park); void save(ParkController park); ParkRepository NONE; }`. `RepositoryException extends RuntimeException` wraps I/O failures, so business code never sees `IOException` or file types. This follows dependency inversion and keeps the layering rule.
- **`ParkStorage implements ParkRepository`** has the constructor `ParkStorage(Path directory, AttractionRepository, GroupRepository, TicketRepository)` (constructor injection) and `static ParkStorage inDirectory(Path)` (configuration code run at initialization). Both normal and `--fly` modes call `inDirectory` with different paths.
- **`ParkController.setRepository(ParkRepository)`** is setter injection, needed because the singleton exists before configuration. `loadState()` and `saveState()` delegate to it. `reset()` restores `ParkRepository.NONE`.
- **Composition root**: `App.configure(Path dataDir)` creates the storage and injects it. The after-command hook calls `park.saveState()` and reports a `RepositoryException` as an error line.
- **Alternatives rejected**: making repositories singletons (forbidden by the source), and a reflection-based DI container (unnecessary; plain Java wiring suffices).
