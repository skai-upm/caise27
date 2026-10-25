# Proposal

## Why

Each controller represents one management responsibility of the park, so there must be exactly one instance of it in the application. At the same time, persistence must stay replaceable (normal files, `--fly` paths, other implementations), and collaborations between controllers must be set up once at startup instead of being built inside operations. This formalises uniqueness with Singleton and collaboration with dependency injection.

## What Changes

- The five controllers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `InvoiceManager`) become Singletons: private constructor, `initialize(...)` receiving all their dependencies, `getInstance()` returning the unique instance.
- Repositories are not Singletons: each repository becomes an interface (`UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository`) with a text-file implementation (`TextFile…Repository`) configured with its path; controllers depend only on the interfaces.
- A startup configuration class (`Bootstrap`) creates the repositories for the chosen data directory (permanent or `--fly`), initialises the controllers injecting repositories, the shared `SystemDate` and the controllers they collaborate with, and returns the command processor. `App` only parses arguments and delegates to it.
- No change in commands, responses or files.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `application-architecture`: "Layer dependencies" names the startup configuration as the only place that wires components; new requirements for unique controllers, injected collaborations and replaceable repositories.

## Impact

- All controllers, repositories (interfaces + implementations), `App`, new `Bootstrap`; tests and fixtures use `initialize`/`TextFile…Repository`; `uml/model.puml` updated.
