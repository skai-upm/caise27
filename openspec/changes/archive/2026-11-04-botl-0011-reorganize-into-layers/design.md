# Design

## Context

All classes are in `es.upm.etsisi.amusementpark`. Today:
- **Presentation-like:** `CommandRunner` (read loop), `CommandProcessor` (split, dispatch, parse dates/prices/fields, format OK/ERROR, HELP/EXIT). `CommandProcessor` also holds `currentDate` and `ticketPrice` and implements cross-entity rules: user deletion checks tickets and cleans up groups, tickets and attractions; the group lock; resolving tickets for attraction entry.
- **Business:** entities (`User`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`), `UserField`, `UserValidator`, `UserException`, and the managers.
- **Persistence:** the `*FileRepository` classes.
- **Wiring:** `App`.

## Goals / Non-Goals

**Goals:**
- Physical separation into packages with one-way dependencies from presentation to business, and from business services to persistence.
- Byte-identical command output and data files.

**Non-Goals:**
- Introducing interfaces, dependency injection or singletons (later F3 proposals cover those).
- Renaming user-facing texts or changing formats.

## Decisions

- **Packages:**
  - `es.upm.etsisi.amusementpark`: `App` only, the composition root. `pom.xml` Main-Class is unchanged.
  - `…presentation`: `CommandRunner`, `CommandProcessor`.
  - `…business.controller`: `ParkController` (new).
  - `…business.service`: `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`.
  - `…business.model`: `User`, `UserField`, `UserValidator`, `UserException`, `Group`, `Ticket`, `IndividualTicket`, `GroupTicket`, `Attraction`.
  - `…persistence`: `UserFileRepository`, `GroupFileRepository`, `TicketFileRepository`, `GroupTicketFileRepository`, `AttractionFileRepository`.
- **Allowed dependencies:**
  - `presentation` → `business.controller`, `business.model` (for results and exceptions).
  - `business.controller` → `business.service`, `business.model`.
  - `business.service` → `business.model`, `persistence`.
  - `persistence` → `business.model` only, because repositories map records to entities.
  - `App` → all, as the composition root.
  - No layer depends on `presentation`. `business.model` depends on no other application package.
- **Extract `ParkController`.** It moves out of `CommandProcessor`: `currentDate`/`ticketPrice` with their getters and setters (the setter rejects non-positive prices), and one method per business operation (`createUser`, `deleteUser` with the ticket check and cleanup, `updateUser`, `createGroup`, `addGroupMember`/`removeGroupMember`/`deleteGroup` with the group lock, `createTicket`, `deleteTicket`, `createGroupTicket`, `deleteGroupTicket`, `createAttraction`, `enterAttraction`, `exitAttraction`, `getAttraction`).
  - `CommandProcessor` keeps the text side: split, operation normalisation, parameter-count checks, parsing of dates, prices and fields, `HELP`/`EXIT`, and output formatting.
  - Error messages are moved verbatim, so outputs stay identical.
  - The startup-date constructor overload moves to `ParkController`, which keeps the only `LocalDate.now()` call.
- **Visibility.** Members previously package-private and used across the new packages (`CommandProcessor.HELP_LINES`, `DEFAULT_TICKET_PRICE`) become `public`. Nothing else is widened.
- **Tests move to the matching packages.** `CommandProcessorTest` obtains the controller through `CommandProcessor.getController()` for date and price assertions. `UmlModelTest` scans the source tree recursively.
- **Architecture test.** `LayeringTest` reads the sources and asserts:
  - every file has a `package` declaration
  - presentation and `business.model` files import neither `…persistence` nor `java.nio.file`
  - `business.model` imports no `…business.service`, `…business.controller` or `…presentation`
  - nothing outside `App` imports `…presentation`

  It uses plain file reading, with no new dependency.
- **Regression evidence.** Before moving code, a reference transcript is captured: a fixed E1/E2 command script run with the current JAR, giving its output and resulting data files. After the refactor, the same script is run with the new JAR and the outputs and files are compared byte for byte (task 4.1).
- **UML.** `uml/model.puml` wraps classes in `package` blocks and adds `ParkController` with its associations to the managers. `CommandProcessor` now holds a `controller` reference instead of four managers, and `currentDate`/`ticketPrice` move to `ParkController`.

## Risks / Trade-offs

- [Large mechanical move] → done with a script that rewrites `package`/`import` lines, followed by compile and full test runs.
- [`persistence` depends on `business.model`] → accepted: the repositories persist model objects. Inverting this would need interfaces, which are not requested now.
