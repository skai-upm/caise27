# Tasks

## 1. Model

- [x] 1.1 Add access-capability interfaces implemented by the tickets, make `Attraction<T extends AttractionAccess>` abstract with typed `admit`, add the four subclasses and `AttractionType`; verify with `AttractionTypesTest` (acceptance matrix at model level, typed admission)

## 2. Commands and persistence

- [x] 2.1 Support `ATTRACTION CREATE` with type (default GENERAL), type-checked ENTER, `Type:` in STATUS and help text; verify the attraction-types scenarios in `AttractionTypeCommandsTest` and keep existing attraction tests passing
- [x] 2.2 Store the type in `attractions.txt` and read untyped lines as GENERAL; update `uml/model.puml`; verify persistence scenarios and `UmlModelTest`

## 3. Milestone commit

- [x] 3.1 Commit the change with the exact message `[F3-C07] Add generic attraction access types`; verify with `git log -1 --format=%s`
