# Spec Delta

## MODIFIED Requirements

### Requirement: ATTRACTION commands listed in HELP
The system SHALL include `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both ATTRACTION command syntaxes

### Requirement: ATTRACTION ENTER listed in HELP
The system SHALL include `ATTRACTION ENTER;<attraction>;<ticket>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`

### Requirement: ATTRACTION EXIT and STATUS listed in HELP
The system SHALL include `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION EXIT and STATUS
- **WHEN** `HELP` is processed
- **THEN** the output contains both command syntaxes
