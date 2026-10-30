# command-interface Specification

## Purpose

Defines how the application receives and interprets text commands, interactively or from a file, including HELP, EXIT and controlled handling of invalid input.

## Requirements

### Requirement: Command syntax
Commands SHALL be lines whose elements are separated by `;`, interpreted with basic String operations including `split`. The recognised commands are `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION LIST`, `ATTRACTION STATUS`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error message is printed and the application keeps running

#### Scenario: Malformed command does not crash
- **WHEN** a line with missing or extra fields for a recognised command is processed
- **THEN** an error message is printed and the application keeps running

### Requirement: Interactive mode
Started without arguments, the application SHALL read commands from standard input, process each one and wait for the next until `EXIT` or end of input.

#### Scenario: Sequence on standard input
- **WHEN** `HELP` then `EXIT` are typed
- **THEN** the help text is shown and the application terminates

### Requirement: File mode
Started with a file path as first argument, the application SHALL process the file's lines in order, ignoring blank lines, using the same command processing as interactive mode. A nonexistent or unreadable file SHALL produce an error message and a controlled termination.

#### Scenario: Commands in order
- **WHEN** a file contains two `USER CREATE` lines, a blank line and a `USER UPDATE` line
- **THEN** both users are created and then updated, and the blank line is ignored

#### Scenario: Missing file
- **WHEN** the application is started with a path that does not exist
- **THEN** an error message is printed and the application terminates without a stack trace

### Requirement: HELP command
`HELP` SHALL print the available commands and their syntax, at least: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP`, `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the twenty command syntaxes is printed

### Requirement: EXIT command
`EXIT` SHALL terminate the application in a controlled manner, and no subsequent command SHALL be processed, including later lines of a command file.

#### Scenario: Lines after EXIT ignored
- **WHEN** a file contains `USER CREATE;...`, `EXIT`, `USER CREATE;...` with valid distinct users
- **THEN** only the first user is created
