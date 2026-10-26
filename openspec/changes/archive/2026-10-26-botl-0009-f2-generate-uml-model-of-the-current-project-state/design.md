# Design

## Context

Current code (package `es.upm.parque`, 19 types) after botl-0008. Existing `docs/uml/model.puml` predates the notation rules. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**
- A diagram an evaluator can check line by line against the fields of each class.

**Non-Goals:**
- Any change to Java code, tests or behavior; adding inverse navigations; showing static constants (implementation detail, not domain state).

## Decisions

- **Derivation rule**: every non-static field whose type is (or contains) an application type becomes a relationship from the declaring class, labelled with the field name; all other non-static fields become attributes `- name : Type`. Static constants are omitted.
- **Collections** (`List<User>`, `Map<String, User>`) → multiplicity `0..*` at the target; single references → `1`, or `0..1` when the code sets them to null (`IndividualTicket.owner`, `GroupTicket.group`).
- **Line styles** (PlantUML): association `A "m" x--> "n" B : field`; aggregation `GroupTicket o--> Group : group`; composition `GroupTicket *--> IndividualTicket : components`; inheritance `<|--`; realisation `<|..`.
- **Aggregation/composition choice** follows the agreed domain semantics from botl-0007 (group ticket aggregates its group, is composed of its internal tickets); all other references are plain associations.
- **Usage dependencies** only for types with no incoming relationship: `CommandProcessor` (created and run by `App`), `UserValidator` (static helper used by the four managers), `UserField` (used by `CommandProcessor` and `UserManager`). `App` itself is the entry point.
- **Location**: `git mv docs/uml/model.puml uml/model.puml`, then rewrite content; `docs/` is removed when empty.

## Risks / Trade-offs

- [Diagram drift in later deliveries] → domain-model requirement obliges updating it with each structural change; a field-by-field check is part of the tasks.
