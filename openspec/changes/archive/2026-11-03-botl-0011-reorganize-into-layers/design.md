# Design

## Context

All 20 production classes are in `es.upm.etsisi.amusementpark`. `CommandProcessor` parses commands, formats responses, holds `currentDate`/`currentPrice`, and performs cross-service rules (USER DELETE blocked by tickets, group locks, cascade of a deleted user out of groups). `App` contains `main`, `--fly` handling, the read loop and the wiring. See proposal.md.

## Goals / Non-Goals

**Goals:** physical separation into layers with clear dependency directions; identical behaviour.

**Non-Goals:** Singleton/dependency-injection formalisation, repository interfaces (F3-C03), any new command or rule.

## Decisions

- **Packages and dependency direction:** `presentation → business.controller → business.model`; `business.controller → persistence`; `persistence → business.model`. `business.model` depends on nothing else in the project. `App` (base package) is the composition root and the only class that knows all layers.
- **Split of `CommandProcessor`:** parsing of command lines, parameter-count checks, text-to-value conversion (dates, decimals), response wording, HELP and the EXIT flag stay in `presentation.CommandProcessor`. Business state and cross-entity rules move to `business.controller.ParkController`: `currentDate`, `currentPrice` (+ validation: non-negative, ≤ 2 decimals), `deleteUser` (authentication, active-ticket check, removal from groups), `addMember/removeMember/deleteGroup` (group lock), `createTicket`, `deleteTicket`, `createGroupTicket`, `deleteGroupTicket`, `enter`, `exit`. Simple operations are delegated to the specific controllers exposed by `ParkController`.
- **Renames:** `UserService`, `GroupService`, `TicketService`, `AttractionService` → `UserController`, `GroupController`, `TicketController`, `AttractionController` — the source places "controllers" in the business layer and the later F3 proposals refer to controllers. Repositories keep their `*FileStorage` names.
- **Read loop:** `runStandardInput`, `runFile`, `run` move from `App` to `presentation.CommandRunner`; `App.execute` still selects mode and data directory.
- **Visibility:** members used across packages become public (e.g. `GroupTicket.constituentId`, `App.createProcessor`, `ParkController.DEFAULT_PRICE`).
- **Tests** move to the package of the class under test; shared fixtures (`CREATE_ANA`, `user(...)`, `groupOf(...)`) go to a public `TestData` class.
- **Regression proof:** a scripted command file is run with the JAR built at the previous commit and with the new JAR on copies of the same data; outputs and data files must be identical.
- **Mandatory milestone message:** the source requires the commit message `[F3-C01] Reorganize application into layered packages`; the batch harness controls the commit subject, so the milestone text is recorded verbatim in the commit body.

## Risks / Trade-offs

- [Large mechanical move] → done with `git mv` in one step, then compile; all existing tests must pass unchanged in substance.
- [ParkController exposing the specific controllers] → keeps simple operations thin; F3-C03 can formalise injection.
