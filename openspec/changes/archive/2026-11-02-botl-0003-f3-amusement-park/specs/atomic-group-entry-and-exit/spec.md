## MODIFIED Requirements

### Requirement: Indivisible group entry
`ATTRACTION ENTER` with a group ticket valid for the current date SHALL admit together all users of the individual tickets in its composition, through the same mechanism used for individual tickets. If the free places are fewer than the users to admit, or any of them is already inside, none SHALL enter. From Delivery 3 group tickets SHALL be accepted only at GENERAL and GROUP attractions.

#### Scenario: Whole group enters
- **WHEN** a group ticket for today has 3 constituents and the attraction has at least 3 free places
- **THEN** all 3 users are inside the attraction

#### Scenario: Insufficient capacity
- **WHEN** a group ticket for today has 4 constituents and the attraction has 3 free places
- **THEN** an error is reported and no member enters

#### Scenario: Group ticket at an INDIVIDUAL or SPECIAL attraction
- **WHEN** a valid group ticket is used at an INDIVIDUAL or SPECIAL attraction
- **THEN** entry is rejected and no member enters
