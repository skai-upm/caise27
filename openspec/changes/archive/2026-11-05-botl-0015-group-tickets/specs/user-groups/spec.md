## ADDED Requirements

### Requirement: Groups locked by active group tickets
A group SHALL be considered locked while at least one of its group tickets is active (date equal to or later than the current system date); the locked state is derived from the tickets and the date, not stored. `GROUP ADD` and `GROUP REMOVE` on a locked group MUST be rejected with an error message starting with `ERROR`. Historical group tickets do not lock the group.

#### Scenario: Active group ticket locks membership
- **GIVEN** group `G01` with an active group ticket
- **WHEN** `GROUP ADD;G01;luis@correo.com` or `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and the membership is unchanged

#### Scenario: Group modifiable again after ticket deleted or historical
- **GIVEN** group `G01` whose only group ticket was deleted with `GROUPTICKET DELETE` or is dated before the current date
- **WHEN** `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** `USER ADDED TO GROUP` is printed

### Requirement: GROUP DELETE command
`GROUP DELETE;<identifier>` SHALL delete the group and print `GROUP DELETED` only when the group has no active group ticket; otherwise it is rejected with an error message starting with `ERROR`. Historical group tickets of a deleted group SHALL be retained with their identifier, date, price and composition, and their group reference becomes empty.

#### Scenario: Group with only historical tickets deleted
- **GIVEN** group `G01` whose group ticket `TG01` is dated before the current date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `GROUP DELETED` is printed, `G01` no longer exists and `TG01` is retained with its composition and price but no group reference

#### Scenario: Group with an active ticket not deleted
- **GIVEN** group `G01` with a group ticket dated today
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error message starting with `ERROR` is printed and `G01` still exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an error message starting with `ERROR` is printed
