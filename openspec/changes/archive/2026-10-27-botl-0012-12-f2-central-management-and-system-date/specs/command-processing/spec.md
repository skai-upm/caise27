# Spec Delta

## ADDED Requirements

### Requirement: HELP lists system commands
The HELP output SHALL also include `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`.

#### Scenario: Help shows system commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>` in addition to the Delivery 1 commands
