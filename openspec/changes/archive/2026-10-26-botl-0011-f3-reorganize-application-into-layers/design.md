# Design

## Context

Single package `es.upm.parque` with 20 types. `CommandProcessor` (≈400 lines) holds parsing, formatting, system date/price and cross-manager rules (user deletion checks, group locks, ticket lookup for attractions). `App` wires storages and managers. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- Packages per layer with one-directional dependencies: presentation → business → persistence, and persistence → business.model (repositories map files to domain objects).
- Move business orchestration out of the presentation class.

**Non-Goals:**
- Renaming repositories, introducing interfaces/DI/singletons (later deliveries), changing behavior or formats.

## Decisions

- **`ParkController`** (business.controller): holds the four managers, `currentDate`, `currentPrice` (`INITIAL_PRICE` 40.00). Methods mirror the commands with typed parameters (`LocalDate`, `BigDecimal`, `UserField`): `createUser`, `deleteUser` (ticket check → delete → group/ticket/attraction cleanup), `updateUser`, `createGroup`, `addGroupMember`/`removeGroupMember`/`deleteGroup` (lock check), `createTicket`, `deleteTicket`, `createGroupTicket`, `deleteGroupTicket`, `createAttraction`, `deleteAttraction`, `enterAttraction`, `exitAttraction`, `getAttraction`, `setDate`, `setPrice` (positive, ≤2 decimals). Static `load(DataDirectory)` builds storages and managers — the composition root moves out of presentation.
- **`CommandProcessor`** keeps text concerns only: `String.split` parsing, parameter counts, text→`LocalDate`/`BigDecimal`/`UserField` conversion, HELP, EXIT, response strings, STATUS formatting, the `run` loop.
- **Validation placement**: `UserValidator` is a domain value rule → `business.model`. Price format (`BigDecimal` parse) is presentation; price business rule (positive, scale ≤ 2) is in `ParkController.setPrice`.
- **Visibility**: constructors used by persistence to restore objects (`IndividualTicket(id, date, price, ownerDni, owner, groupTicketId)`, `GroupTicket(..., components)`) and `GroupTicket.componentId` become public, documented as restoration/identifier helpers.
- **Mechanics**: `git mv` files (history preserved), rewrite `package` lines and explicit imports with a script, then hand-edit `CommandProcessor`/`ParkController`; tests mirror the main packages.

## Risks / Trade-offs

- [Large mechanical diff] → behavior protected by the existing 127 tests, moved alongside, plus a manual E1/E2 run.
- [Presentation `App` still selects the data location (`DataDirectory`)] → only the location choice (`--fly`); all file access stays in persistence and wiring in `ParkController.load`.
