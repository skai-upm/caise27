# Spec Delta

## ADDED Requirements

### Requirement: HELP lists group commands
The HELP output SHALL also include `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>` and `GROUP DELETE;<Identifier>`.

#### Scenario: Help shows group commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains the four GROUP command syntaxes
