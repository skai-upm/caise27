## MODIFIED Requirements

### Requirement: All persistence redirected
During a `--fly` execution every repository (users, groups, tickets, group tickets, attractions with their type, passes with their price and consumed status, and any other persistent element) SHALL read and write only files inside the temporary directory, through the same repository implementations as a normal execution; the permanent data files SHALL NOT be read or modified.

#### Scenario: Permanent data untouched
- **GIVEN** permanent data files with existing users
- **WHEN** a `--fly` run creates, updates and deletes users, groups, tickets and attractions
- **THEN** the permanent data files are byte-for-byte unchanged after the run

#### Scenario: Independent starting state
- **GIVEN** the permanent data contains user `ana@correo.com`
- **WHEN** a `--fly` run processes `USER CREATE` for a user with email `ana@correo.com`
- **THEN** the creation succeeds, because the temporary data starts empty

#### Scenario: F3 data isolated
- **GIVEN** permanent data files
- **WHEN** a `--fly` run creates typed attractions, buys and consumes passes and deletes tickets
- **THEN** the permanent files are unchanged, no permanent `passes.txt` is created, and the temporary directory is removed at the end
