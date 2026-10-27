# Design

## Context

Model classes live in `es.upm.amusementpark.model`; the singleton controller in `park`; repositories in `repository`.

## Decisions

- **Package-level layout** (`model`, `park`, `repository`, `persistence`, `command`) so the diagram mirrors the code organisation.
- **Attributes** use the code's field names (`firstName`, `surnames`, `dni`, …) so they can be checked mechanically.
- **Arrows** only in the navigable directions implemented in the code; `UserValidator` is shown as a utility using `User`.
- **`UmlModelTest`** parses `uml/model.puml` lines: collects `class|abstract class|enum <Name>` declarations and `<type> <field>` attribute lines inside each class body, and verifies against reflection on `es.upm.amusementpark.model` (and `ParkController`).
