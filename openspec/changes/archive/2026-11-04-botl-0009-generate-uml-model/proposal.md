# Proposal

## Why

`uml/model.puml` has been maintained incrementally by earlier changes and mixes notations (methods, usage arrows everywhere, `{readOnly}` labels, relationship names that differ from the attribute names such as `owner`/`buyer`). This change regenerates it as a faithful, valid PlantUML class diagram of the code as it is now, following the notation rules of the source, without touching application behaviour.

## What Changes

- Rewrite `uml/model.puml` from the current Java sources (`com.parque`): all classes, the abstract `Ticket` hierarchy (`IndividualTicket`, `GroupTicket`) and the `UserField` enumeration.
- Attributes: only primitive-like values (strings, numbers, booleans, dates, amounts, paths) and enumerations; no technical labels (`{readOnly}`, `{static}`); static constants are not shown.
- Every object-valued field is drawn as a relationship (association, aggregation or composition), never as an attribute; its label is exactly the field name; the direction follows the class that holds the field; multiplicities are given where the code allows inferring them; no inverse relationships are added.
- Navigability marking: plain associations use a cross at the holder end and an arrow at the referenced end (`x-->`); aggregations/compositions put the diamond at the holder end and an arrow at the referenced end.
- No methods are shown in classes, and usage (dependency) arrows are drawn only towards/for classes that have no incoming relationship (`App`, `CommandProcessor`, `UserValidator`, `UserField`).
- `UmlModelTest` is extended to check the diagram against the code by reflection (each relationship label is a field of the holder class whose type is the target or a list of it).
- No production code changes.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `uml-model`: "PlantUML class model" now specifies the notation rules above and requires the diagram to match the implementation exactly.

## Impact

- **Files**: `uml/model.puml` rewritten; `src/test/java/com/parque/UmlModelTest.java` extended.
- **Behaviour**: unchanged (production sources untouched; full test suite must still pass).
- **Conflict resolved**: the source asks for "relevant public methods" but its acceptance criteria say "Do not represent usage relationships or methods in classes". The specific acceptance criterion is applied (no methods); usage relationships appear only where the criterion "If a class has no incoming relationships, its usage must be shown" requires them.
- **Interpretations**: `LocalDate`, `BigDecimal` and `Path` are value types and are shown as attributes; the abstract class is marked `abstract`; `App`'s static configuration constants are not shown.
