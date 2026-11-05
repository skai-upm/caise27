# Design

## Context

Production types (21) live in `es.upm.amusementpark`, `.cli`, `.park` and `.persistence`. There is no `User` class: users are identified by e-mail and resolved through `UserDirectory`. See proposal.md - Why.

## Decisions

- **One diagram with PlantUML `package` blocks**, using fully qualified names so identically named elements can't collide. Attributes are taken from the private fields that matter to the model (identifiers, dates, prices, references).
- **Represent users honestly**: `String userEmail`/member e-mail sets are shown as attributes, with a note. A `User` class is not drawn because it doesn't exist in the code. The user-model milestone must update the diagram.
- **Consistency test** (`UmlModelTest`, JUnit, plain String operations): it walks `src/main/java`, derives each type's kind from its declaration (`interface`, `abstract class`, `class`), and checks that the diagram declares `<kind> <SimpleName>`. It also checks the key edges literally. PlantUML rendering isn't available in the build (no external libraries), so syntax is checked structurally by the delimiters.
