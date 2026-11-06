# Spec Delta

## MODIFIED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `GROUP CREATE;<id>;<name>`, `GROUP ADD;<id>;<email>`, `GROUP REMOVE;<id>;<email>`, `GROUP DELETE;<id>`, `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>`, `TICKET DELETE;<id>`, `GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>`, `GROUPTICKET DELETE;<id>`, `ATTRACTION CREATE;<id>;<name>;<capacity>`, `ATTRACTION LIST;<id>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION STATUS;<id>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<group>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<price>`, `HELP` and `EXIT`, and MUST NOT change any stored data.

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the command syntaxes listed above and processing continues with the next line

#### Scenario: Help in file mode
- **GIVEN** a command file whose only line is `HELP`
- **WHEN** the application processes it
- **THEN** the same help text as in interactive mode is printed
