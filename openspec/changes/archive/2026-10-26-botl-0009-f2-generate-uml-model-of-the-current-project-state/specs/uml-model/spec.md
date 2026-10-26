# Spec Delta

## ADDED Requirements

### Requirement: Model reflects the implementation
`uml/model.puml` SHALL be derived from the current code: every element (class, interface, enumeration, attribute, relationship) SHALL exist in the implementation, inverse relationships SHALL NOT be added unless they exist in the code, and the ticket hierarchy SHALL match the implemented classes. The model SHALL be processable by PlantUML.

#### Scenario: Nothing invented
- **WHEN** each class, attribute and relationship in `uml/model.puml` is compared with the source code
- **THEN** each one corresponds to an existing class or field

#### Scenario: Valid PlantUML
- **WHEN** PlantUML checks `uml/model.puml`
- **THEN** it reports no syntax error

### Requirement: Attributes are primitive values only
The model SHALL show as attributes only fields of primitive/value types (numbers, booleans, strings, dates, amounts, paths) or enumerations, without technical labels such as `{readOnly}` or stereotypes; a field that references another model class SHALL be shown only as a relationship.

#### Scenario: Relationship not duplicated as attribute
- **WHEN** the `Group` class is inspected
- **THEN** it shows `id` and `name` as attributes and its members only as the `members` relationship to `User`

### Requirement: Relationship notation
Each association, aggregation or composition SHALL be named after the field that holds it in the owning class, SHALL show the navigation direction with an arrow towards the referenced class and a cross at the owning end, and SHALL show the multiplicity at the referenced end. Methods and usage relationships SHALL NOT be shown, except that a class with no incoming relationship SHALL be shown with the usage that reaches it.

#### Scenario: Named navigable relationship
- **WHEN** the relationship from `Attraction` to `User` is inspected
- **THEN** it is named `present`, has a cross at `Attraction`, an arrow at `User` and multiplicity `0..*`

#### Scenario: Usage only for classes without incoming relationships
- **WHEN** the model is inspected
- **THEN** classes such as `UserValidator` and `UserField` appear with the usage that reaches them, and no other usage relationships are shown
