# Design

## Context

Flat package `es.upm.parque` with: entry point `App`; command handling `CommandProcessor`, `CommandRunner`; domain `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `SystemDate`; rules `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `UserValidator`, `UserException`; storage `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `TemporaryDataDirectory`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- Mechanical move with explicit imports; names that state each class's role.

**Non-Goals:**
- Interfaces for repositories, Singleton/DI (next changes); any behavior change.

## Decisions

- **Mapping**
  - `es.upm.parque`: `App` (composition root; keeps the JAR manifest `mainClass` unchanged).
  - `es.upm.parque.presentation`: `CommandProcessor`, `CommandRunner`.
  - `es.upm.parque.business.model`: `User`, `UserField`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`, `SystemDate`.
  - `es.upm.parque.business.controller`: `UserController`, `GroupController`, `TicketController`, `AttractionController` (renamed from `*Manager`; fields `userManager` → `userController`, etc.).
  - `es.upm.parque.business.validation`: `UserValidator`.
  - `es.upm.parque.business`: `BusinessException` (renamed from `UserException`).
  - `es.upm.parque.persistence`: `UserRepository`, `GroupRepository`, `TicketRepository`, `AttractionRepository`, `TemporaryDataDirectory`.
- **Dependencies**: presentation → business; business.controller → persistence (to save) and business.model; persistence → business.model (to build objects). Presentation never imports persistence; only `App` wires repositories.
- **Visibility**: members used across packages become `public` where they were package-private (none currently besides `App.buildProcessor`, which stays in `App`).
- **Execution**: done with `git mv` plus a script that rewrites `package` lines, renames identifiers, and inserts explicit imports for referenced project classes. Verified by comparing the output of a full command script run before and after.

## Risks / Trade-offs

- [Large diff] → purely mechanical; guarded by the unchanged test suite and the before/after output comparison.
