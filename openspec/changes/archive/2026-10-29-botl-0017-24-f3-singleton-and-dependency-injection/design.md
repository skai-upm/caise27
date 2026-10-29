# Design

## Context

`ParkController` (singleton) holds all state; `ParkStorage` (persistence) loads/saves it and is called from `App` through `CommandProcessor.setAfterCommand`. `ParkController.createUserInvoice/createGroupInvoice` do `new InvoiceDirector()` per call.

## Goals / Non-Goals

**Goals:** dependency inversion for repositories, explicit injection at start-up, singleton controllers, injected collaborations. **Non-Goals:** new behavior; DI framework.

## Decisions

- **`business.repository`**: interfaces `UserRepository` (`saveAll(Collection<User>)`, `List<User> loadAll(List<String> skipped)`), `GroupRepository`, `TicketRepository`, `AttractionRepository` (`saveAll(...)`, `loadInto(ParkController, List<String> skipped)`), and record `Repositories(users, groups, tickets, attractions)`.
- **`persistence`**: `TextFileRepository` base; `TextFileUserRepository`, `TextFileGroupRepository`, `TextFileTicketRepository`, `TextFileAttractionRepository` implement the interfaces (formats unchanged); `TextFileRepositories.create(Path)` builds a `Repositories` for one directory. `ParkStorage` is removed (its load/save order moves to `ParkController`).
- **`ParkController`**: `configure(Repositories)` (injection), `List<String> load()` (users → groups → attractions → tickets), `persist()` (no-op if unconfigured). `reset()` clears state and the configuration.
- **`InvoiceController`** (singleton, holder idiom): `configure(ParkController, InvoiceDirector)`; `createUserInvoice(email)`, `createGroupInvoice(id)`; uses `ParkController` public queries. Unconfigured use falls back to the singleton park and a default director only via an explicit `configureDefaults()` called by the convenience wiring.
- **`CommandProcessor(PrintStream, ParkController, InvoiceController)`** plus convenience constructor `(PrintStream)` wiring the singletons (default configuration).
- **`App`** (composition root): choose directory → `TextFileRepositories.create(dir)` → `park.reset(); park.configure(repos); park.load()` → `invoices.configure(park, new InvoiceDirector())` → `new CommandProcessor(out, park, invoices)` with `afterCommand = park::persist`.

## Risks / Trade-offs

- [Repository interface takes the controller for reconstruction] → keeps cross-reference rules in business; flagged in proposal.
