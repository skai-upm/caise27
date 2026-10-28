# execution-control Specification

## Purpose
Defines the general session-control commands of the Amusement Park application: HELP to list available commands and EXIT to terminate in a controlled way, consistently for every line-based command source.

## Requirements

### Requirement: HELP command
The system SHALL accept the command `HELP` and display the available commands and their basic syntax, including at least the lines `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP` and `EXIT`.

#### Scenario: Help lists mandatory syntax
- **WHEN** the command `HELP` is processed
- **THEN** the output contains each of the lines `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP` and `EXIT`

#### Scenario: Help does not end the session
- **WHEN** the commands `HELP` and then `HELP` are processed from the same source
- **THEN** the help listing is displayed twice

### Requirement: EXIT command
The system SHALL accept the command `EXIT` and terminate command processing in a controlled manner; once `EXIT` is processed no subsequent command from the same source SHALL be executed.

#### Scenario: Exit stops processing of subsequent lines
- **WHEN** the lines `HELP`, `EXIT`, `HELP` are processed in that order
- **THEN** the help listing is displayed exactly once and processing ends after `EXIT`

#### Scenario: Exit terminates normally
- **WHEN** the application reads `EXIT` from standard input
- **THEN** the application ends without error

### Requirement: Source-independent command semantics
The system SHALL process commands one per line with the same behaviour regardless of whether the lines come from standard input or from a file.

#### Scenario: Same result for interactive and file-like sources
- **WHEN** the same sequence of command lines is processed once as standard input and once as the contents of a file
- **THEN** both runs produce the same output and stop at the same `EXIT` line

### Requirement: Unrecognised command reporting
The system SHALL report an error message for a non-empty line that does not match any available command and continue processing the following lines.

#### Scenario: Unknown command
- **WHEN** the lines `FOO` and `HELP` are processed
- **THEN** an error message is displayed for `FOO` and the help listing is displayed afterwards
