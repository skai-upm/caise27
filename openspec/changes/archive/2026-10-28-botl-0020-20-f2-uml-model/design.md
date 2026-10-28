# Design

## Context

The design now has packages `model`, `repository`, `service`, `persistence`, `park`, `cli`. See proposal.md.

## Goals / Non-Goals

**Goals:** an accurate, reviewable diagram; a cheap guard against drift.
**Non-Goals:** rendering images in the build (no extra tooling/dependencies).

## Decisions

- **Scope**: all `model` types with attributes; `ParkController` (singleton) with its services and repositories; `UserStorage`/`UserFileStorage` and the other storage classes; `ActiveGroupTicketCheck`. CLI/App shown as a thin layer.
- **Packages** mirror Java packages so the diagram matches the code structure.
- **Consistency test** (`UmlModelConsistencyTest`): reads `uml/model.puml`, collects names declared with `class`/`abstract class`/`interface`/`enum`/`record`, and asserts (a) each exists as a class in its package, (b) every top-level type in `es.upm.amusementpark.model` is declared. Implemented with `String` operations (test code).

## Risks / Trade-offs

- [The test checks declarations, not every arrow] → relationships reviewed manually against fields when writing the diagram.
