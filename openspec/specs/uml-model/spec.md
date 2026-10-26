# uml-model Specification

## Purpose
Keeps a PlantUML class model of the implemented object-oriented design in the repository, consistent with the code.

## Requirements

### Requirement: PlantUML class model file
The repository SHALL contain a valid PlantUML class diagram at `uml/model.puml` that represents the implemented classes, enumerations, relationships and multiplicities.

#### Scenario: Model includes groups
- **WHEN** `uml/model.puml` is inspected after groups are implemented
- **THEN** it shows the group class, its association to users with multiplicity `0..*`, and the classes that manage and persist groups

#### Scenario: Model includes tickets
- **WHEN** `uml/model.puml` is inspected after tickets are implemented
- **THEN** it shows the ticket class with its reference to exactly one user, and the classes that manage and persist tickets

#### Scenario: Model includes the ticket hierarchy
- **WHEN** `uml/model.puml` is inspected after group tickets are implemented
- **THEN** it shows the abstract ticket with its individual and group subclasses, the aggregation from group ticket to group, and the composition from group ticket to its individual tickets with multiplicities

#### Scenario: Model includes attractions
- **WHEN** `uml/model.puml` is inspected after attractions are implemented
- **THEN** it shows the attraction class with its collection of present users (`0..*`) and the classes that manage and persist attractions

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

### Requirement: Model shows layer packages
`uml/model.puml` SHALL group classes in the same packages as the code (presentation, business model, business controllers, business validation, persistence).

#### Scenario: Packages in the diagram
- **WHEN** `uml/model.puml` is inspected after the layered reorganisation
- **THEN** each class appears inside the package that contains it in the code

### Requirement: Model shows the invoice Builder
`uml/model.puml` SHALL show the invoice builder interface, its implementation, the director and the invoice product with their relationships.

#### Scenario: Builder in the diagram
- **WHEN** `uml/model.puml` is inspected after invoices are implemented
- **THEN** it shows the builder interface implemented by the concrete builder, the director, the invoice with its sections and lines, and the controller that uses them

### Requirement: Model shows singletons and repository abstractions
`uml/model.puml` SHALL mark Singleton controllers and SHALL show the repository interfaces with their persistence implementations, with controllers associated to the interfaces.

#### Scenario: Repository realisation
- **WHEN** `uml/model.puml` is inspected after dependency injection is introduced
- **THEN** each file repository realises its business repository interface and each controller is associated with the interface, and controllers are stereotyped `<<Singleton>>`

### Requirement: Model shows the help structure
`uml/model.puml` SHALL show the help generator, the help format interface with its implementations, and the command catalog with its command descriptors.

#### Scenario: Help strategy in the diagram
- **WHEN** `uml/model.puml` is inspected after extended help is implemented
- **THEN** it shows the help format interface realised by the concise and extended formats, and the help generator associated with one format and one catalog

### Requirement: Model shows password encoding
`uml/model.puml` SHALL show the password encoder abstraction, its BCrypt implementation and the user controller's association with the abstraction.

#### Scenario: Encoder in the diagram
- **WHEN** `uml/model.puml` is inspected after password hashing is introduced
- **THEN** it shows the encoder interface realised by the BCrypt encoder and associated from the user controller

### Requirement: Model shows passes
`uml/model.puml` SHALL show the abstract pass with its SpeedPass and Special Pass subclasses, their composition in the individual ticket with multiplicities `0..*` and `0..1`, and the pass controller and repository abstraction.

#### Scenario: Passes in the diagram
- **WHEN** `uml/model.puml` is inspected after passes are implemented
- **THEN** it shows `IndividualTicket` composed of `0..*` SpeedPasses and `0..1` Special Pass
