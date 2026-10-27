# Spec Delta

## ADDED Requirements

### Requirement: HELP lists typed attraction creation
HELP and HELP EXTENDED SHALL also include `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, while still showing the Delivery 2 form `ATTRACTION CREATE;<identifier>;<name>;<capacity>`.

#### Scenario: Help shows both creation forms
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` and `ATTRACTION CREATE;<identifier>;<name>;<capacity>`
