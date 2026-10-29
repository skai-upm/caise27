# Spec Delta

## ADDED Requirements

### Requirement: Help covers all deliveries
`HELP` SHALL show the syntax of the commands of all three deliveries, including at least `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, the SpeedPass and Special Pass commands, both `INVOICE CREATE` forms, `HELP`, `HELP EXTENDED` and `EXIT`; `HELP EXTENDED` SHALL describe each of them.

#### Scenario: Complete help
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every listed syntax appears, and in the extended help each is followed by its description
