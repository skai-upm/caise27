# Spec Delta

## ADDED Requirements

### Requirement: Diagram matches the Delivery 3 implementation
The class diagram SHALL be valid PlantUML and SHALL represent every implemented class, interface and enumeration of Delivery 3 (layers, controllers and repositories, invoices, help, passes, generic attractions), with one relationship line per model-typed field of the code and no element absent from the code.

#### Scenario: Field-by-field check
- **WHEN** each field of the production classes whose type is a model class (or a collection of them) is compared with the diagram
- **THEN** each has exactly one relationship line named after it, and every production type appears in the diagram
