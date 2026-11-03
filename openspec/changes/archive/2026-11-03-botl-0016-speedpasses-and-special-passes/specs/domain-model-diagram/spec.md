# Spec Delta

## ADDED Requirements

### Requirement: Passes in the diagram
The class diagram SHALL show the pass hierarchy and each pass's association with its individual ticket, plus the pass controller and repository.

#### Scenario: Pass hierarchy shown
- **WHEN** the diagram is reviewed
- **THEN** `SpeedPass` and `SpecialPass` specialise `Pass`, and `Pass` is associated with exactly one `Ticket`
