# Design

## Context

All 15 production classes are in `es.upm.amusementpark`; business methods throw `IllegalArgumentException`; `TextFileRepository` throws `UncheckedIOException`; `App` creates and deletes the `--fly` directory itself.

## Goals / Non-Goals

**Goals:** packages per layer; exception hierarchy; enforce dependencies by a test; unchanged behavior.

**Non-Goals:** Singleton/DI refinements (F3-C03), new features.

## Decisions

- **Package map**: `presentation.CommandProcessor`; `business.model.{User, Group, Ticket, IndividualTicket, GroupTicket, Attraction}`; `business.control.ParkController`; `business.exception.{ParkException, ElementNotFoundException, DuplicateElementException, InvalidValueException, OperationNotAllowedException}`; `persistence.{TextFileRepository, UserRepository, GroupRepository, TicketRepository, AttractionRepository, ParkStorage, PersistenceException, DataDirectories}`; `App` in the root package (composition root, `Main-Class` unchanged).
- **Exceptions**: `ParkException extends RuntimeException`; the four subclasses map the existing failures (not found / already exists or in use / invalid date, price, capacity, blank, past date / rule violations such as locked group, undeletable ticket, empty group, wrong date, no capacity). Messages are kept identical so behavior is unchanged. `presentation` defines `InvalidCommandException` for wrong argument counts. `PersistenceException extends RuntimeException` wraps I/O errors. `CommandProcessor` catches `ParkException` and `InvalidCommandException` → `Error: <message>`; `run` keeps a last-resort `RuntimeException` guard. `App` catches `PersistenceException` at start-up.
- **`DataDirectories`** (persistence): `permanent(base)`, `createTemporary(base)` and `delete(dir)` — `App` no longer touches the file system itself.
- **Visibility**: members now used across packages become public (`ParkController.reset`, `registerUser`, reconstruction methods `resolveUser/restoreGroup/restoreMember/restoreTicket`, `GroupTicket.releaseGroup`).
- **Enforcement**: `ArchitectureTest` reads each source file's `package` and `import` lines and asserts the dependency rules and that business sources do not import `java.nio.file`/`java.io.File*`.
- **Tests** stay in the root test package (they exercise several layers) and import the new packages.

## Risks / Trade-offs

- [Wide mechanical change] → compile + full test suite + JAR run guard behavior preservation.
