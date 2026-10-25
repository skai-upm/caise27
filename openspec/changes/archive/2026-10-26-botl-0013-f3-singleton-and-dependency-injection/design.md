# Design

## Context

Controllers are created with `new` in `App.start` and in the `Park` test fixture, receiving concrete repositories (`UserRepository` etc. are classes with a `Path`). `App` also parses arguments and handles `--fly`.

## Goals / Non-Goals

**Goals:**
- Singleton controllers whose dependencies are injected once at startup.
- Repository abstractions to allow different implementations/configurations.

**Non-Goals:**
- A DI framework; Singleton for `CommandProcessor` (presentation, not a management controller) or for repositories.

## Decisions

- **Singleton with initialisation**: each controller has `private static X instance`, a private constructor, `public static X initialize(deps…)` that builds the unique instance from the injected dependencies (loading its data) and `public static X getInstance()` (throws `IllegalStateException` if not initialised). Calling `initialize` again replaces the instance: the application calls it once at startup, while tests reinitialise for isolation. Alternative: lazy `getInstance()` with setter injection — rejected because it allows half-configured controllers.
- **Repository abstractions**: interfaces keep the existing names (`UserRepository`, …) with the same `load`/`save` signatures; implementations `TextFileUserRepository`, … keep the current code. Both live in `persistence`; controllers import only the interfaces.
- **`Bootstrap`** (package `es.upm.parque`, composition root): `static CommandProcessor configure(Path dataDir, LocalDate today)` builds the text-file repositories under `dataDir`, the `SystemDate`, initialises controllers in dependency order (users → groups → tickets → attractions → invoices) injecting collaborators, and returns the processor wired with `getInstance()` results. `App.start` calls it; `--fly` only changes `dataDir`.
- **`Park` fixture** uses `Bootstrap.configure` and exposes the singletons, so tests run against the real wiring.
- **Tests**: `SingletonTest` checks identity and reinitialisation; `InMemoryUserRepository` (test code) proves controllers accept alternative implementations.
- **UML**: repository interfaces with realisations from the `TextFile…` classes; `Bootstrap` and its usage lines; the static `instance` fields are not instance attributes and are not drawn.

## Risks / Trade-offs

- [Global mutable state complicates parallel tests] → Surefire runs tests sequentially (default); each test reinitialises the controllers it uses.
