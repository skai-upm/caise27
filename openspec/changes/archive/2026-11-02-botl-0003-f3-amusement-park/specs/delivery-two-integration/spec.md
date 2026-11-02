# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions (Java 21, Maven, executable JAR, no databases, frameworks or external libraries other than the BCrypt dependency allowed from Delivery 3) and SHALL represent loaded domain relationships with object references and collections in the specified directions: attractions know their users, individual tickets know their user, group tickets compose their individual tickets and reference their group while it exists, and users and groups reference no tickets. (F2-C10)

#### Scenario: Relationship directions
- **WHEN** the domain classes are inspected
- **THEN** relationships use object references in exactly those directions and no user or group holds ticket or attraction references
