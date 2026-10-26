# Proposal

## Why

`uml/model.puml` was grown incrementally with each change and mixes conventions (technical labels such as `{readOnly}`/`{static}`/`<<utility>>`, static constants as attributes, usage arrows for some classes but not others). Delivery 2 asks for a class diagram that faithfully documents the current implementation under explicit modelling rules, without touching the code.

## What Changes

- Regenerate `uml/model.puml` from an analysis of the current source code, documenting the existing design only:
  - all relevant domain and architecture classes, the abstract `Ticket` hierarchy, and the `UserField` enumeration;
  - inheritance, associations, aggregation and composition with their real navigability (no inverse relationships added) and multiplicities where the code allows inferring them;
  - attributes limited to primitive/value types and enumerations; references to other classes appear only as relationships, never as attributes;
  - relationship names equal to the attribute name in the class that holds the reference, and an `x` on the non-navigable end so every line shows its direction and which class holds the attribute;
  - no methods, no usage relationships, and no technical labels on attributes — except that a class with no incoming relationship shows its usage.
- No production or test code changes.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `design-model`: the class model requirements are made explicit — faithful to the current code, modelling conventions for attributes, relationship naming and direction, no methods or usage relationships except for classes without incoming relationships, and no invented elements.

## Impact

- `uml/model.puml` only. Verified with PlantUML; `git diff` must show no change under `src/`.
