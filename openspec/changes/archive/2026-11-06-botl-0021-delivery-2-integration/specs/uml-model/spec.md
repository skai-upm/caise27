# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram consistent with the code
Every class, enumeration and inheritance relationship in the diagram SHALL exist in the code with the same name, kind and supertype, and every association drawn MUST correspond to a field of the source class whose type matches the target directly or as a collection element.

#### Scenario: Automated consistency check
- **WHEN** the test suite runs
- **THEN** a test parses `uml/model.puml` and verifies its classes, enumerations, abstract markers, inheritance and associations against the compiled classes, failing on any mismatch
