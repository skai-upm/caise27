# Spec Delta

## ADDED Requirements

### Requirement: Delivery 2 technical restrictions
The application SHALL keep the Delivery 1 restrictions and represent relationships with objects and collections in memory in the required directions: attractions know their users but users do not know attractions; individual tickets know their user but users do not store tickets; group tickets are composed of individual tickets and reference their group while it exists; groups and users do not reference tickets. Ticket types SHALL share a common abstraction, and a single central controller instance SHALL exist.

#### Scenario: Relationship directions in the code
- **WHEN** the fields of `User`, `Group`, `IndividualTicket`, `GroupTicket` and `Attraction` are inspected
- **THEN** `User` has no ticket, group or attraction fields; `Group` has no ticket fields; `IndividualTicket` has a `User` field; `GroupTicket` has a `Group` field and a collection of `IndividualTicket`; `Attraction` has a collection of `User`

#### Scenario: Common ticket abstraction
- **WHEN** the ticket classes are inspected
- **THEN** `IndividualTicket` and `GroupTicket` both extend the abstract `Ticket`
