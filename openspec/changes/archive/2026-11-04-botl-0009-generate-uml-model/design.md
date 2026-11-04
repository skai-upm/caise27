# Design

## Context

The code base has 22 production types in `es.upm.etsisi.amusementpark`, and a diagram already exists at `docs/uml/model.puml`. That diagram shows methods, visibility marks, `{readOnly}`/`{static}` labels, many dependency arrows, and labels that do not always match field names (for example `owner` for `IndividualTicket.user`). The source requires `uml/model.puml` and stricter notation.

## Goals / Non-Goals

**Goals:**
- A diagram derived field by field from the code.
- Notation exactly as the delivery's acceptance list states.

**Non-Goals:**
- Any refactoring or behaviour change.
- Showing intended or future design.

## Decisions

- **Conflict: "relevant public methods" vs. "do not represent ... methods in classes".** The acceptance list's notation bullets are the more specific instruction and come last, so classes show no methods. This decision is recorded here, as the brief asks, instead of silently dropping one instruction.
- **Conflict: "do not represent usage relationships" vs. "if a class has no incoming relationships, its usage must be shown".** These are read together: no usage arrows in general, but a class that would otherwise have no incoming relationship gets dashed `..>` arrows from the classes that use it:
  - `UserValidator`: used by `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`
  - `UserField`: used by `CommandProcessor`, `UserManager`
  - `UserException`: thrown by managers, `Attraction`, `CommandProcessor`
  - `CommandRunner`: used by `App`

  `App` is the entry point and nothing uses it, so it stands alone.
- **What counts as an attribute.** `String`, `int`, `boolean`, `LocalDate`, `BigDecimal` and `Path` are treated as datatypes (value types outside the model) and shown as attributes. `UserField` is an enum, but no class stores it as a field. Fields holding model objects or collections/maps of them become relationships. Static constants are omitted as technical details.
- **Relationship kinds** (holder → referenced : field):
  - Compositions `*-->`: the managers own their entities (`UserManager.users`, `GroupManager.groups`, `TicketManager.tickets`, `TicketManager.groupTickets`, `AttractionManager.attractions`), and `GroupTicket.parts`.
  - Aggregations `o-->`: `Group.members`, `Attraction.present`, `GroupTicket.group`.
  - Associations `x-->`, with the cross at the holder end: `IndividualTicket.user`, the controller's references to managers, the managers' references to repositories and `UserManager`, and `CommandRunner.processor`.
- **Multiplicities.**
  - Collections are `0..*`, except `GroupTicket.parts`, which is `1..*` because purchase rejects empty groups.
  - Nullable references (`IndividualTicket.user`, `GroupTicket.group`) are `0..1`, and other single references are `1`.
  - Holder-side multiplicities are given where the code implies them (for example a `User` can be in `0..*` groups).
- **Location.** The diagram moves to `uml/model.puml` and `docs/uml/model.puml` is removed, so there is one model to maintain.
- **Guard test.** `UmlModelTest` lists `src/main/java/es/upm/etsisi/amusementpark/*.java` and asserts each type name is declared in `uml/model.puml` (as `class`, `abstract class`, `interface` or `enum`). It also checks the `@startuml`/`@enduml` delimiters. Full PlantUML parsing is verified with the PlantUML jar (`-syntax`) during the integration task, because adding PlantUML as a test dependency is not required by the source.

## Risks / Trade-offs

- [Treating `String`/`LocalDate`/`BigDecimal`/`Path` as datatypes] → these are standard value types. Drawing them as classes would add elements that are not part of the project.
- [Holder-side multiplicities are partly interpretive] → only given where the code's rules make them clear.
