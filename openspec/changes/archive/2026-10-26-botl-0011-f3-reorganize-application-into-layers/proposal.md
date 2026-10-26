# Proposal

## Why

All classes live in a single package `es.upm.parque`, mixing terminal interaction, domain rules and file access. Delivery 3 requires a layered structure that physically separates presentation, business and persistence responsibilities, as the base for the design patterns introduced next.

## What Changes

- Move classes into layer packages, keeping behaviour identical:
  - `es.upm.parque.presentation` — `App` (start-up and terminal/file input) and `CommandProcessor` (command interpretation).
  - `es.upm.parque.business.model` — `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `SystemDate`.
  - `es.upm.parque.business.controller` — the domain-rule classes, renamed from `*Manager` to `*Controller` (`UserController`, `GroupController`, `TicketController`, `AttractionController`).
  - `es.upm.parque.business.validation` — `UserValidator`.
  - `es.upm.parque.persistence` — `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `TemporaryDataDirectory`.
- Tests move to the matching packages; an architecture test checks the layering rules.
- The JAR manifest main class becomes `es.upm.parque.presentation.App`.
- `uml/model.puml` is updated with packages and renamed classes.
- Command syntax, responses and file formats are unchanged.

## Capabilities

### New Capabilities
- `application-architecture`: Structural rules of the code base (layer packages and allowed responsibilities).

### Modified Capabilities
- `uml-model`: the model must show the layer packages.

## Impact

- Every Java file moves package (history kept with `git mv`); four classes renamed; `pom.xml` main class updated.
- No change to commands, outputs, persisted data or dependencies.
