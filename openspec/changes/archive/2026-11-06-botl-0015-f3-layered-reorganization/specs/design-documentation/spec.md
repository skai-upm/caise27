# Spec Delta

## MODIFIED Requirements

### Requirement: Diagram corresponds to the code
Every class, interface and record of the business layer (model, controller and exception packages) and of the persistence layer SHALL appear in the diagram, grouped by layer package, and the diagram SHALL NOT show model elements that do not exist in the code.

#### Scenario: Missing class is detected
- **GIVEN** a class is added to the model package without updating the diagram
- **WHEN** the test suite runs
- **THEN** the UML consistency check fails, naming the missing class

#### Scenario: Diagram uses layer packages
- **WHEN** the diagram is inspected
- **THEN** its packages are `es.upm.park.business.*`, `es.upm.park.persistence` and `es.upm.park.presentation`
