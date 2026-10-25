# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
`HELP` SHALL display the syntax of every registered command: the USER, SYSTEM, GROUP, TICKET, GROUPTICKET, ATTRACTION, INVOICE, SPEEDPASS and SPECIALPASS commands, `HELP`, `HELP EXTENDED` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `INVOICE CREATE`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `HELP`, `HELP EXTENDED` and `EXIT`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, for every registered command, its syntax and a brief description of its behavior. Both help modes SHALL be generated from the same registered command information, by a help component that receives its output-formatting behavior through dependency injection.

#### Scenario: Extended help
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command from `HELP` appears with its syntax followed by a description
