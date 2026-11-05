# Spec Delta

## MODIFIED Requirements

### Requirement: Controlled handling of failing commands
Abnormal situations SHALL be represented by exceptions: malformed commands by an invalid-command exception from the presentation layer, and operations that business rules refuse by a park exception from the business layer. These exceptions SHALL propagate to command processing, which SHALL handle them in one place by reporting an error message for that line and continuing with the following lines. If executing a command fails unexpectedly in any other way, the application SHALL report an error message for that line and continue processing; the session SHALL NOT terminate abnormally.

#### Scenario: Failure inside one command
- **GIVEN** a command whose execution raises an unexpected error, followed by `HELP`
- **WHEN** the input is processed
- **THEN** an error message is shown for the failing line
- **AND** `HELP` is still executed

#### Scenario: Malformed command
- **GIVEN** `GROUP CREATE;G01` (missing a field) followed by `HELP`
- **WHEN** the input is processed
- **THEN** `ERROR: Invalid syntax. Usage: GROUP CREATE;<Identifier>;<Name>` is shown and `HELP` is still executed

#### Scenario: Operation refused by a business rule
- **GIVEN** group `G01` exists, and `GROUP CREATE;G01;Otra` is followed by `HELP`
- **WHEN** the input is processed
- **THEN** `ERROR: Group G01 already exists` is shown, `G01` is unchanged, and `HELP` is still executed
