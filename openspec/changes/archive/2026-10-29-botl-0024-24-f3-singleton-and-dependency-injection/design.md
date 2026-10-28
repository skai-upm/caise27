# Design

## Context

`ParkController` (already a singleton) creates repositories and `new`s every service in `reset`. Services have public constructors. See proposal.md.

## Goals / Non-Goals

**Goals:** singleton controllers with injection; composition root owning creation of repositories/storages; behavior unchanged.
**Non-Goals:** repository interfaces per technology; DI framework.

## Decisions

- **Services as singletons**: `private` constructor, `private static final X INSTANCE`, `getInstance()`, and `configure(...)` (returns `this`) taking repositories and collaborating controllers. Fields are set by `configure`.
- **`ParkDependencies`** record (business): user/group/ticket/attraction repositories, `UserStorage`, and optional `ParkStorage` (group/ticket/attraction file storages, in `persistence.file`).
- **`ParkInitializer`** (business, composition root): `inMemory(UserStorage)`, `forLocation(DataLocation)` build `ParkDependencies` with new instances; `wire(ParkController, ParkDependencies)` configures every singleton: users ← (userRepo, userStorage); tickets ← (ticketRepo, users, date/price suppliers from the park); groups ← (groupRepo, users, tickets::hasActiveGroupTicket, date supplier); attractions ← (attractionRepo); invoices ← (users, groups, tickets); park ← (all of them + dependencies).
- **`ParkController.initialize(...)`** methods call `ParkInitializer`, then load stored data into the injected repositories.

## Risks / Trade-offs

- [Singleton services hold state between initializations] → every initialization reconfigures them with fresh repositories.
