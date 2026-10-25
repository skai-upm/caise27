# Proposal

## Why

All 22 production classes live in a single package, mixing command interaction, business rules and file access. Delivery 3 starts by physically separating presentation, business and persistence responsibilities into distinct Java packages, so later design work (Builder, Singleton, dependency injection, generics) builds on a clear layered structure — without changing any existing command.

## What Changes

- Package `es.upm.parque.presentation`: command-based interaction (`CommandProcessor`).
- Package `es.upm.parque.business.model`: domain model and domain rules (`User`, `UserField`, `UserValidator`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `SystemDate`).
- Package `es.upm.parque.business.controller`: controllers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`).
- Package `es.upm.parque.persistence`: repositories and file access (`*Repository`, `TemporaryDirectory`).
- `es.upm.parque.App` stays as the application entry point and wiring; the default package is not used.
- Persistence depends only on the model: value parsing used by repositories (`parsePrice`, `parseCapacity`) moves from controllers to model classes.
- Command syntax, responses and stored file formats are unchanged; tests are moved to the matching packages; `uml/model.puml` shows the packages.

## Capabilities

### New Capabilities
- `application-architecture`: The layered package structure and the allowed dependencies between layers.

### Modified Capabilities
<!-- None: no externally observable behavior changes. -->

## Impact

- Every production and test source file moves package (imports updated); a few members become `public` to be reachable across packages.
- `uml/model.puml` and `UmlModelTest` updated for packages. `mvn package` still builds `target/parque.jar` with main class `es.upm.parque.App`.
