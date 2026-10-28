## ADDED Requirements

### Requirement: Delivery 2 relationship directions
Relationships SHALL be held as object references and collections in memory with the mandated directions: attractions reference their occupants, individual tickets reference their user, group tickets compose their individual tickets and reference their group (nullable), while users hold no references to tickets, groups or attractions and groups hold no references to tickets. All ticket types SHALL share a common abstraction, and a single park controller instance SHALL exist.

#### Scenario: Users and groups do not reference tickets or attractions
- **WHEN** the fields of the user and group model classes are inspected
- **THEN** no field refers to tickets or attractions, and the user class has no field referring to groups

#### Scenario: Ticket abstraction
- **WHEN** the ticket model classes are inspected
- **THEN** individual and group tickets extend the same abstract ticket type

### Requirement: Delivery 2 end-to-end execution
A command file combining Delivery 1 and Delivery 2 commands SHALL execute in file order through the packaged application in normal mode and in `--fly` mode, with rejected operations reported and processing continuing, and `--fly` leaving no generated data behind.

#### Scenario: Full Delivery 2 command file
- **WHEN** a command file with system, group, ticket, group ticket and attraction commands (including invalid ones) and `EXIT` is executed with and without `--fly`
- **THEN** both runs produce the same output, the normal run persists its state, and the `--fly` run leaves no temporary directory
