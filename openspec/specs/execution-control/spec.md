# execution-control Specification

## Purpose
Provides the general session-control commands of the Amusement Park application: HELP to describe the available commands and their syntax, and EXIT to end the session in a controlled way, identically for interactive and file input.

## Requirements

### Requirement: HELP lists available commands and syntax
The system SHALL respond to the command `HELP` by displaying the available commands with their basic syntax, including at least `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`.

#### Scenario: HELP in interactive mode
- **GIVEN** the application reading commands from standard input
- **WHEN** the user enters `HELP`
- **THEN** the output contains the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT
- **AND** the application keeps waiting for the next command

#### Scenario: HELP from a command file
- **GIVEN** a command file whose first line is `HELP`
- **WHEN** the file is processed
- **THEN** the output is the same help text as in interactive mode
- **AND** processing continues with the next line

#### Scenario: HELP shows the complete Delivery 2 syntax
- **WHEN** `HELP` is processed
- **THEN** every syntax line listed in this requirement appears verbatim in the output

### Requirement: EXIT terminates the session in a controlled manner
The system SHALL respond to the command `EXIT` by ending command processing normally, with no error, and SHALL NOT read or execute any command that follows it, whatever the input source.

#### Scenario: EXIT in interactive mode
- **GIVEN** the application reading commands from standard input
- **WHEN** the user enters `EXIT`
- **THEN** the application terminates normally without waiting for more input

#### Scenario: Commands after EXIT in a file are not executed
- **GIVEN** a command file containing `HELP`, then `EXIT`, then `HELP`
- **WHEN** the file is processed
- **THEN** the help text is displayed exactly once
- **AND** the line after `EXIT` is not executed

#### Scenario: End of input without EXIT
- **GIVEN** an input source that ends without an `EXIT` command
- **WHEN** all lines have been processed
- **THEN** the application terminates normally

### Requirement: Same command behavior for every input source
The system SHALL process each command line through the same command handling whether it comes from standard input or from a command file given as the first program argument, producing the same observable output.

#### Scenario: Identical output for both sources
- **GIVEN** the lines `HELP` and `EXIT`
- **WHEN** they are processed once from standard input and once from a file
- **THEN** both runs produce identical output

### Requirement: Unknown commands are rejected without terminating
The system SHALL report a line that is not a recognized command with an error message and SHALL continue processing the following commands.

#### Scenario: Unrecognized command
- **GIVEN** the application processing commands
- **WHEN** the line `FOO` is processed, followed by `EXIT`
- **THEN** an error message identifying an unknown command is displayed
- **AND** the following `EXIT` command is still executed
