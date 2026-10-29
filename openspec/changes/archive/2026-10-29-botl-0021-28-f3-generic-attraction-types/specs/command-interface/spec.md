# Spec Delta

## ADDED Requirements

### Requirement: HELP lists typed attraction creation
The HELP and HELP EXTENDED outputs SHALL include `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` in addition to the Delivery 2 syntax.

#### Scenario: HELP shows typed creation
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`
