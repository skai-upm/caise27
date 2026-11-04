# Spec Delta

## MODIFIED Requirements

### Requirement: Single date source
The application SHALL hold exactly one current date, owned by the business-layer park controller that executes every command, and every time-dependent business rule SHALL obtain the date from it rather than from the computer's clock.

#### Scenario: Change affects subsequent operations immediately
- **GIVEN** the current date is changed with `SYSTEM DATE`
- **WHEN** the next command is executed by the same park controller
- **THEN** that command observes the newly configured date
