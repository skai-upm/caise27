# Spec Delta

## ADDED Requirements

### Requirement: Model reflects the Delivery 3 design
`uml/model.puml` SHALL represent the Delivery 3 classes and relationships as implemented: the layer packages, the personal pass hierarchy and its composition in individual tickets, the access-capability interfaces and their implementation by the ticket classes, the generic attraction class with its bounded type parameter and its specialisations, the invoice builder, the help strategy, and the singleton controllers with their injected dependencies.

#### Scenario: Delivery 3 elements present
- **WHEN** the model is inspected
- **THEN** it contains `Pass`, `SpeedPass`, `SpecialPass`, `Admission`, `IndividualAdmission`, `GroupAdmission`, `Attraction<T extends Admission>` with its four subclasses, `AttractionType`, `InvoiceBuilder` and `HelpFormatter`, connected as in the code

#### Scenario: Cross-check with the code
- **WHEN** every association in the model is compared with the fields of the source code
- **THEN** each association corresponds to a field of the holding class and every field referencing a model class appears as an association
