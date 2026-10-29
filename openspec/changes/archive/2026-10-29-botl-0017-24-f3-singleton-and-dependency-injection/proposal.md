# Proposal

## Why

Milestone F3-C03 requires controllers with a unique management responsibility to apply Singleton, repositories to be replaceable dependencies created and injected during application initialization (never global singletons), and collaborations between controllers or services to be configured by injection at initialization instead of being created inside operations — with project code only, no DI framework. Today `ParkController` is a singleton but knows nothing about repositories (the entry point saves through a separate storage object), and it creates a new `InvoiceDirector` inside every invoice operation.

## What Changes

- Repository abstractions (`UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, grouped in `Repositories`) live in the business layer; the text-file implementations live in the persistence layer.
- The entry point (composition root) creates the text-file repositories for the selected directory (permanent or `--fly` temporary) and injects them into central park management, which loads its state from them and saves through them. Normal and `--fly` modes differ only in the configured directory; business logic is unchanged.
- Invoice generation moves to an `InvoiceController` singleton whose collaborators (central park management and the invoice director) are injected at initialization.
- `CommandProcessor` receives the controllers it uses through its constructor.
- No externally observable behavior changes.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `architecture`: singleton controllers, injected replaceable repositories, injected collaborations.

## Impact

- New `business.repository` package and `business.control.InvoiceController`; persistence classes renamed to `TextFile*Repository` and `TextFileRepositories` factory replaces `ParkStorage`; `App`, `CommandProcessor`, tests and `uml/model.puml` updated.

## Ambiguities (flagged)

- Repository interfaces receive the central controller when loading so they can rebuild cross-element references (group members, ticket users) through its reconstruction methods; this keeps reconstruction rules in the business layer.
- `CommandProcessor` keeps a convenience constructor wired to the singleton controllers (used by unit tests); the application itself injects explicitly.
