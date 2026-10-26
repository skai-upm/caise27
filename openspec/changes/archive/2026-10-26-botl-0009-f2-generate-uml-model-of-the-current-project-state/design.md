# Design

## Context

The model is plain PlantUML text in `uml/model.puml`; it is validated with `plantuml -checkonly` (PlantUML jar from Maven Central). The code lives in package `es.upm.parque` (20 types). This change must not modify code.

## Goals / Non-Goals

**Goals:** a diagram faithful to the fields in the code, following the stated conventions.

**Non-Goals:** documenting methods, JDK types, or functional-interface callbacks.

## Decisions

- **Source of truth**: instance fields of each class. Fields typed with model classes (or collections of them) become relationships named after the field; other fields become attributes `name : Type` (no visibility, no `{readOnly}`, no stereotypes).
- **Notation**: association `Owner x--> "mult" Target : field`; aggregation for `GroupTicket.group` (`o-->`, the group exists independently and is shared); composition for `GroupTicket.tickets` (`*-->`, internal tickets live and die with the group ticket); inheritance `Ticket <|-- IndividualTicket/GroupTicket`. Owner-end crosses mark which class holds the field; diamonds already mark the owner for aggregation/composition.
- **Multiplicities**: single reference → `1`; `List` → `0..*`; `GroupTicket.tickets` → `1..*` (a group ticket always has at least one member ticket).
- **Excluded fields**: `UserManager.deletionChecks` and `GroupManager.modificationChecks` (lists of JDK `Function` callbacks, not model classes) and static constants.
- **Usage for classes without incoming relationships**: `App ..> CommandProcessor` (App creates it in a local variable), usages of `UserValidator` (from `UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`) and of `UserField` (from `CommandProcessor`, `UserManager`). `App` itself is the entry point and has no user.
- **Abstract class** `Ticket` is marked `abstract`; `UserField` is an `enum` with its literals.

## Risks / Trade-offs

- [The model can drift from code again] → later changes that alter classes must update it (existing `uml-model` requirement).
