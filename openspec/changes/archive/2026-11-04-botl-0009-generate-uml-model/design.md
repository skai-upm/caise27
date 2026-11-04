# Design

## Context

There are 20 production types in `es.upm.park`. The existing `docs/uml/model.puml` shows methods, `{readOnly}`/`<<utility>>` labels, usage arrows for classes that already have associations, and `App ..> CommandRunner`. The source requires the model at `uml/model.puml`. See proposal.md.

## Goals / Non-Goals

**Goals:**
- A diagram derived field by field from the code, following the acceptance-criteria notation.

**Non-Goals:**
- Any change to production code, new classes or inverse relationships.

## Decisions

- **Methods conflict:** the description lists "relevant public methods", but the acceptance criteria say "Do not represent usage relationships or methods in classes". The acceptance criteria are the more specific and verifiable rule, so no methods are shown. Polymorphism (`getHolders()`) is still conveyed through the `Ticket` inheritance. The conflict is recorded here rather than silently dropping either instruction.
- **What counts as an attribute:** "objects should only have primitives and enumerations as attributes" is read as "domain objects are relationships". `String`, `LocalDate`, `BigDecimal` and `Path` are value types, not modelled classes, so they are kept as attributes. Otherwise `User` would show no data at all. Static constants (`USAGE`, `DEFAULT_PRICE`, file paths in `App`) are omitted as implementation details, and so is the nested `App.DataFiles` record.
- **Relationship derivation** (holder attribute → target, multiplicity from the field type):
  - `CommandRunner.processor` → `CommandProcessor` (1)
  - `CommandProcessor.users/groups/tickets/attractions` → the managers (1 each)
  - `UserManager.storage` → `UserFileStorage` (1)
  - `UserManager.users` → `User` (0..*)
  - `GroupManager.storage/users/groups` → `GroupFileStorage` (1), `UserManager` (1), `Group` (0..*)
  - `Group.members` → `User` (0..*)
  - `TicketManager.storage/groupStorage/tickets/groupTickets` → `TicketFileStorage` (1), `GroupTicketFileStorage` (1), `IndividualTicket` (0..*), `GroupTicket` (0..*)
  - `IndividualTicket.owner` → `User` (0..1)
  - `GroupTicket.group` → `Group` (0..1, aggregation)
  - `GroupTicket.constituents` → `IndividualTicket` (1..*, composition)
  - `AttractionManager.storage/attractions` → `AttractionFileStorage` (1), `Attraction` (0..*)
  - `Attraction.present` → `User` (0..*)
- **Navigability notation:** plain associations use `Holder "m" x--> "n" Target : attribute`. The cross marks the holder end as non-navigable from the target, and the arrow points at the referenced class. Aggregation and composition use `o-->` / `*-->`: the diamond sits on the holder end and the arrow gives the direction.
- **Usage arrows:** only `UserValidator` (used by `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`), `UserField` (used by `UserManager`, `CommandProcessor`) and `CommandRunner` (used by `App`) lack incoming relationships, so only they get dashed usage arrows. `App` is the entry point, used by nobody; a note says so.
- **Sync test:** `UmlModelTest` lists `src/main/java/es/upm/park/*.java` and compares them with the `class|abstract class|enum|interface` declarations in `uml/model.puml`. It also asserts the document has no `(`-style operations, `<<`/`{` labels inside class bodies, and is wrapped in `@startuml`/`@enduml`. PlantUML syntax is checked with the PlantUML jar during verification (not a project dependency).

## Risks / Trade-offs

- [Reading of "primitives" is interpretive] → Documented above; it can be tightened without code changes.
- [Removing `docs/uml/model.puml` changes a documented path] → Superseded by the required `uml/model.puml`; spec updated accordingly.
