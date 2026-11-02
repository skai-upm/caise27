# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions (Java 21, Maven, executable JAR via `mvn package`, interactive and command-file execution, no databases, persistence frameworks or external libraries other than the BCrypt dependency authorized in Delivery 3). In-memory domain relationships SHALL be object references and collections, not identifiers or strings, with these directions: attractions know their occupants; individual tickets know their user; group tickets are composed of individual tickets and reference their group while it exists; users and groups do not reference tickets.

#### Scenario: Restriction inspection
- **WHEN** `pom.xml` and the domain classes are inspected
- **THEN** the only runtime dependency is the authorized BCrypt library and the relationships follow the stated directions
