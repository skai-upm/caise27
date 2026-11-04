# Design

## Context

There are 22 types in `es.upm.park`.
- `CommandProcessor` (about 400 lines) parses commands but also holds the managers, `currentDate` and `currentPrice`, and applies the rules: the active-ticket check before `USER DELETE`, the group lock before `GROUP ADD/REMOVE/DELETE`, user/group lookups for tickets, and attraction entry with the date.
- `App` builds every `*FileStorage`, owns `DataFiles` and drives `--fly` through `TemporaryDataDirectory`.

See proposal.md.

## Goals / Non-Goals

**Goals:**
- Physical layer separation with verifiable dependency rules; zero behaviour change.

**Non-Goals:**
- Singleton/DI changes, invoices, help changes and other F3 features; renaming commands or files.

## Decisions

- **Split `CommandProcessor`:**
  - `presentation.CommandProcessor` keeps `String.split` parsing, parameter counts, text-to-value parsing (`yyyy-MM-dd`, price and capacity format), HELP/EXIT and reply formatting (including the exact `OK TICKET CREATED -> ...` literals).
  - `business.service.ParkController` receives typed arguments and holds the managers, the single `currentDate` and `currentPrice` (40.00) and every cross-cutting rule. Its methods include `deleteUser`, `addGroupMember`, `createTicket(id, email, date)`, `enterAttraction(aid, tid)` and `setPrice(BigDecimal)`.
  - Alternative of moving `CommandProcessor` wholesale to business was rejected: parsing is presentation.
- **Price rule placement:** format validation (digits, at most 2 decimals) stays in presentation; positivity (> 0) moves to `ParkController.setPrice`. Same messages and outcomes.
- **`ParkSession` (business.service, `AutoCloseable`):**
  - `permanent(Path workDir)` builds storages from `DataFiles.in(workDir)`.
  - `temporary(Path tmpRoot, LocalDateTime start)` creates the run directory, registers the shutdown hook and builds on it; `close()` deletes it.
  - `App` uses try-with-resources, so presentation never imports persistence.
  - The `--fly` parsing stays in `App` (a launch option is presentation).
- **`App.run(commandArgs, ParkController, ...)`** replaces the `DataFiles` overload. `AppTest` drives everything through `App.launch(args, workDir, ...)` with per-test directories.
- **Visibility:** members previously package-private but now used across packages become `public`: `User.copyEditableFrom`, `Group.add/remove/copy`, `IndividualTicket` history constructor, `GroupTicket.of`. No behaviour change.
- **Tests:** each test moves to the package of the class it tests. Shared fixtures are duplicated where the tests now sit in different packages.
  - `LayeringTest` scans `src/main/java` for package declarations and imports to enforce the requirements.
  - `UmlModelTest` compares class → package between code and diagram.
- **Manifest:** `mainClass` becomes `es.upm.park.presentation.App`.
- **Mandatory milestone message** `[F3-C01] Reorganize application into layered packages` is recorded verbatim in the commit body; the batch tool fixes the commit subject format.

## Risks / Trade-offs

- [Large mechanical move] → Done in compilable steps, with the existing ~237 tests as the safety net.
- [business.service ↔ persistence both reference business.model] → Intended direction; there is no cycle between model and persistence.
