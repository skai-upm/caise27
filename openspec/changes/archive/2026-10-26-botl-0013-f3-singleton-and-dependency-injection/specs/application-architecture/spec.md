# Spec Delta

## ADDED Requirements

### Requirement: Business depends on repository abstractions
The business layer SHALL declare the repository abstractions it needs and SHALL NOT depend on persistence-layer implementations; the persistence layer SHALL implement those abstractions.

#### Scenario: No persistence imports in business
- **WHEN** the business-layer sources are inspected
- **THEN** none imports a class from the persistence package
