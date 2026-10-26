# Spec Delta

## ADDED Requirements

### Requirement: Model shows passes
`uml/model.puml` SHALL show the abstract pass with its SpeedPass and Special Pass subclasses, their composition in the individual ticket with multiplicities `0..*` and `0..1`, and the pass controller and repository abstraction.

#### Scenario: Passes in the diagram
- **WHEN** `uml/model.puml` is inspected after passes are implemented
- **THEN** it shows `IndividualTicket` composed of `0..*` SpeedPasses and `0..1` Special Pass
