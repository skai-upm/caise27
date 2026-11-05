# Tasks

## 1. Model

- [x] 1.1 Write `uml/model.puml` covering all packages, the ticket hierarchy, group-ticket composition, associations with directions and multiplicities, interface implementations, and the user note; verify it against the current sources

## 2. Consistency guard

- [x] 2.1 Add `UmlModelTest` checking delimiters, every production type with its kind, and the key relationships; verify it passes and fails when a type is missing (temporarily removing one declaration)

## 3. Verification

- [x] 3.1 Run `mvn -q package` and verify all tests pass
