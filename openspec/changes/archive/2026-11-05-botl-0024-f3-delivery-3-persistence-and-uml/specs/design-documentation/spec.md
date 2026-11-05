# Spec Delta

## MODIFIED Requirements

### Requirement: Model consistent with the implementation
The class model SHALL correspond to the code: every production class and interface SHALL be declared with its kind, and no element absent from the code SHALL be drawn as a class. In particular, it SHALL show the ticket hierarchy (`IndividualTicket` and `GroupTicket` extending abstract `Ticket`), the composition of a group ticket with its individual tickets (multiplicity 1..*), the one-way, optional reference from a group ticket to its group, and how users, groups, tickets and attractions relate. For Delivery 3 it SHALL also show the access interfaces implemented by tickets, the generic attraction class with its typed subclasses, the attraction type enumeration, the relationship between individual tickets and passes with its cardinalities (any number of SpeedPasses, at most one Special Pass per ticket), the invoice Builder abstractions and concrete builders, the relationships created by dependency injection, and the singleton controller.

#### Scenario: Every production type is drawn
- **GIVEN** the production sources under `src/main/java`
- **WHEN** they are compared with `uml/model.puml`
- **THEN** each class, abstract class and interface appears in the diagram

#### Scenario: Key relationships drawn
- **WHEN** `uml/model.puml` is inspected
- **THEN** it contains `Ticket <|-- IndividualTicket`, `Ticket <|-- GroupTicket`, a composition from `GroupTicket` to `IndividualTicket` with multiplicity `1..*`, and a directed association from `GroupTicket` to `Group` with multiplicity `0..1`

#### Scenario: Model drift detected
- **GIVEN** a new production class that is not in the diagram
- **WHEN** the test suite runs
- **THEN** the consistency check fails and names the missing class

#### Scenario: Delivery 3 elements drawn
- **WHEN** `uml/model.puml` is inspected
- **THEN** it contains `GeneralAccess`, `IndividualAccess` and `GroupAccess`, `Attraction<T extends GeneralAccess>` with its four subclasses, `AttractionType`, `IndividualTicket "1" -- "0..*" SpeedPass` and `IndividualTicket "1" -- "0..1" SpecialPass`, `InvoiceBuilder` with `UserInvoiceBuilder` and `GroupInvoiceBuilder`, the injected `ParkRepository` and `PasswordHasher`, and `ParkController <<singleton>>`
