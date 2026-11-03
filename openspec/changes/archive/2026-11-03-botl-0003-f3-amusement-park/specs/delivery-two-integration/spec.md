# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions; in memory, domain relationships SHALL be object references and collections (not identifiers or strings) with the specified directions; tickets SHALL share a common abstraction and group tickets SHALL use composition; `--fly` SHALL reuse the normal repositories. From Delivery 3, passes reference their individual ticket, and BCrypt is the only permitted external runtime library.

#### Scenario: Relationship directions
- **WHEN** the domain classes are inspected
- **THEN** attractions reference users, individual tickets reference users, group tickets compose individual tickets and reference their group, passes reference individual tickets, and neither users nor groups reference tickets or attractions
