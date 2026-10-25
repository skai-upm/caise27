# Design

## Context

`uml/model.puml` exists from earlier proposals with visibility marks, `{readOnly}` labels, a `<<utility>>` stereotype and three generic `..>` lines. The code has 21 production classes/enums in `es.upm.parque` (see the field inventory gathered during proposal: managers hold repositories, other managers, `SystemDate` and maps of domain objects; domain classes hold users/groups/tickets in lists).

## Goals / Non-Goals

**Goals:**
- A diagram that strictly mirrors fields and inheritance in the code, with the notation rules of the spec.
- An automated consistency check so later proposals cannot drift silently.

**Non-Goals:**
- Any refactoring or change to production code; representing methods, local variables or method-level collaborations.

## Decisions

- **Relationship extraction rule**: each non-static field whose type (or element/value type of a `List`/`Map`) is a model class becomes one relationship named after the field. Multiplicity at the target: `1` for single references, `0..*` for collections (`1..*` for `GroupTicket.tickets`, never empty by construction). Multiplicity at the holder: `1` for owning managers/controllers; `0..*` where many holders can reference the same target (e.g. `IndividualTicket.holder`, `Group.members`, `Attraction.present`, `GroupTicket.group`).
- **Kinds**: maps/lists of domain objects owned by a manager → aggregation `o-->` (objects outlive structural changes, are loaded from repositories); `GroupTicket.tickets` → composition `*-->` (created and destroyed with the group ticket); `Group.members`, `Attraction.present` → aggregation; all other single references → association `x-->`.
- **Value types** `String`, `int`, `boolean`, `LocalDate`, `BigDecimal`, `Path` are attributes. Static constants are not shown.
- **Usage lines** only for classes with no incoming relationship: `App` (entry point, shown as `App ..> CommandProcessor` because it creates and runs it), `UserValidator` and `UserField` (used by managers).
- **`UmlModelTest`** (JUnit, reflection): parses `uml/model.puml`; for each relationship line `A "m" <op> "n" B : f` asserts `A` declares field `f` whose type is `B` or a `List`/`Map` of `B`; for each attribute `name : Type` asserts the field exists and is not a model type; asserts every class in `es.upm.parque` main sources appears in the diagram and that every non-static field referencing a model class has a relationship line. Lives in test code only.

## Risks / Trade-offs

- [Reflection test couples to the notation] → Intended: it enforces the conventions for P19's update.
