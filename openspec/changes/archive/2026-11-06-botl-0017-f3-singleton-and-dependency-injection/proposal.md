# Proposal

## Why

Milestone F3-C03 requires controllers with a unique, execution-wide responsibility to apply Singleton, repositories to be replaceable dependencies created and injected during initialization (never global singletons), and collaborations between controllers to be configured by injection instead of being created inside operations. Today `ParkController` is a singleton and receives its storage by setter, but `TextParkStorage` creates its own repositories internally. Invoicing is mixed into `ParkController`, and the command processor looks controllers up by itself, so no controller collaboration is configured explicitly.

## What Changes

- Repositories (`GroupRepository`, `TicketRepository`, `AttractionRepository`) are created during application initialization for the configured data directory and injected into `TextParkStorage` through its constructor. Normal and `--fly` mode differ only in the paths given to these repositories.
- Add an application configuration step (`AppConfiguration`, project code, no DI framework) that builds the repositories and storage, injects them into the controllers, wires controller collaborations, and creates the command processor with its controllers.
- Extract invoice generation into an `InvoiceController` singleton (a unique invoicing responsibility) whose collaborator `ParkController` is injected at initialization.
- The presentation `CommandProcessor` receives the controllers it uses through its constructor.
- Behavior and output of all commands are unchanged.

### In scope
- Singleton controllers, repository injection, controller collaboration injection, configuration code, UML update.

### Out of scope
- User controller and user repository (user milestones later in this batch). They will follow the same configuration pattern.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `layered-architecture`: adds requirements for single-instance controllers, injected (non-singleton) repositories, and collaborations configured at initialization.

## Impact

- `persistence.TextParkStorage` constructor. New `es.upm.park.AppConfiguration`. `App` uses it.
- New `business.controller.InvoiceController`. `ParkController` loses the invoice methods and exposes read-only queries (`groupTicketsOf`, `userDni`).
- `presentation.CommandProcessor` constructor injection (the no-argument constructor delegates to the singletons for convenience).
- UML model and tests.
