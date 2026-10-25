# Design

## Context

All classes are in `es.upm.parque`. Cross-class static references today: controllers use each other's response constants (package-private), `CommandProcessor` uses `UserManager.USER_DELETED`, and repositories call `TicketManager.parsePrice` / `AttractionManager.parseCapacity` (persistence → controller, which the layering must forbid). Tests share fixtures through package-private statics (`UserManagerTest.ana()`, `CommandProcessorTest.CREATE_ANA`, `Park`…). `UmlModelTest` assumes one package.

## Goals / Non-Goals

**Goals:**
- Pure move refactoring with minimal visibility changes; identical observable behavior.

**Non-Goals:**
- Renaming classes, Singleton/DI (next proposals), changing file formats.

## Decisions

- **Package map**: as listed in the proposal. `App` remains in `es.upm.parque` as the bootstrap/composition root, keeping the JAR manifest `Main-Class` unchanged; it is the only class that knows both presentation and persistence.
- **Value parsing moves to the model**: `TicketManager.parsePrice` → `Ticket.parsePrice` (public static), `AttractionManager.parseCapacity` → `Attraction.parseCapacity`. Controllers delegate to them.
- **Visibility**: response constants used across packages (`UserManager.USER_DELETED`, …) and all controller constants become `public static final`; package-private test hooks (`App.run/start/launch`, `CommandProcessor.COMMAND_SYNTAX`) stay package-private and their tests move with them.
- **Tests mirror packages**: model tests → `business.model`, controller tests → `business.controller`, repository tests → `persistence`, `CommandProcessorTest` → `presentation`, `AppTest`/`UmlModelTest`/fixtures stay in `es.upm.parque`. Shared fixtures go to a public `es.upm.parque.Fixtures` (sample users, command strings, timestamps) and the public `Park` fixture.
- **Mechanics**: a one-off script moves files, rewrites `package` lines and adds explicit imports (no wildcards) for the referenced classes of other packages; then compile errors are fixed by hand.
- **Architecture test** `LayeringTest` scans source imports to enforce the dependency rules of the spec.
- **UML**: classes grouped in nested `package` blocks; `UmlModelTest` resolves class names through a simple-name → fully-qualified-name map built from the source tree.

## Risks / Trade-offs

- [Large mechanical diff] → Behavior is guarded by the unchanged test suite plus an end-to-end command file compared before/after.
