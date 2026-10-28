# Spec Delta

## ADDED Requirements

### Requirement: Relationships as object references
While loaded in memory, domain relationships SHALL be object references and collections, not identifiers or strings, with these directions: attractions reference the users inside them; individual tickets reference their user; group tickets are composed of their individual tickets and reference the purchasing group while it exists; groups reference their member users. Users SHALL NOT reference attractions or tickets, and groups SHALL NOT reference tickets.

#### Scenario: Group members are users
- **WHEN** Ana is added to group `G01` and then changes her email with `USER UPDATE`
- **THEN** `G01` still contains the same user object, now with the new email, and `GROUP REMOVE;G01;<new email>` removes her
