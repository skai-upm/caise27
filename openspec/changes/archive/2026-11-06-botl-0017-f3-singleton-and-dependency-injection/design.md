# Design

## Context

`ParkController` is a singleton with setter-injected `UserDirectory` and `ParkStorage`. `App` creates `TextParkStorage(paths, err)`, which internally creates the three repositories. Invoice direction lives in `ParkController`. `CommandProcessor` calls `ParkController.getInstance()` in every handler.

## Goals / Non-Goals

**Goals:** explicit composition at startup, non-singleton repositories injected by constructor, and one demonstrable controller-to-controller collaboration wired by injection.
**Non-Goals:** a generic DI container, or splitting the park controller further.

## Decisions

- **`AppConfiguration`** (root package, next to `App`): `configure(Path dataDir, PrintStream warnings)` creates `StoragePaths`, the three repositories and `TextParkStorage`. It injects the storage into `ParkController`, injects `ParkController` into `InvoiceController`, and returns the configured `CommandProcessor`. It is plain Java code, with no framework or reflection.
- **Constructor injection for persistence**: `TextParkStorage(GroupRepository, TicketRepository, AttractionRepository)`. Repositories keep public constructors and have no static state.
- **`InvoiceController`** is a singleton (eager holder, private constructor) with `setParkController(ParkController)`. Setter injection is needed because both are singletons created by their classes. `userInvoice`/`groupInvoice` move here unchanged in behavior. Without a configured collaborator they throw `IllegalStateException("InvoiceController is not configured")`. They never fall back to `ParkController.getInstance()`.
- **`ParkController` read-only queries** used by invoicing: `groupTicketsOf(Group)` (now public) and `userDni(email)`.
- **`CommandProcessor(ParkController, InvoiceController)`**. Handlers use the injected fields. The no-argument constructor delegates to the singletons, so existing tests keep working. `App` uses the configured instance.
- **Singletons are not extended to persistence**: tests assert that two configurations produce distinct repository instances.

## Risks / Trade-offs

- [Setter injection between singletons can be forgotten] → The missing configuration fails fast with a clear error, and a test covers it.
