# command-interface Specification

## Purpose

Defines how the Amusement Park application receives and processes text commands from its input sources, and the general HELP and EXIT commands that control execution.

## Requirements

### Requirement: Source-independent command processing
The application SHALL process commands one per line, in order, through a single command-processing mechanism regardless of whether the lines come from standard input or from a command file, so that the observable result of a command is the same for both sources.

#### Scenario: Same output from both sources
- **WHEN** the same sequence of command lines is processed once from standard input and once from a file
- **THEN** the produced output is identical

#### Scenario: Interactive mode without arguments
- **WHEN** the application is started without arguments
- **THEN** it reads commands from standard input and, after processing each command, waits for the next one

#### Scenario: File given as first argument
- **WHEN** the application is started with a file path as its first argument
- **THEN** it processes the commands contained in that file

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least the Delivery 2 minimum command set exactly as spelled in the scenario below, and SHALL NOT stop processing.

#### Scenario: HELP lists the minimum command set
- **WHEN** the line `HELP` is processed
- **THEN** the output contains each of `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`

#### Scenario: HELP lists the SYSTEM commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `SYSTEM DATE;<yyyy-MM-dd>` and `SYSTEM PRICE;<Price>`

#### Scenario: HELP lists the GROUP commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>` and `GROUP DELETE;<Identifier>`

#### Scenario: HELP lists the TICKET commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`

#### Scenario: HELP does not stop processing
- **WHEN** `HELP` is followed by another command line
- **THEN** the following command is also processed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner, and once EXIT is processed no subsequent line of the input SHALL be processed.

#### Scenario: EXIT in file mode stops later lines
- **WHEN** a file containing `HELP`, then `EXIT`, then `HELP` is processed
- **THEN** the help text is produced exactly once and the line after `EXIT` is not executed

#### Scenario: EXIT from standard input
- **WHEN** `EXIT` is read from standard input
- **THEN** the application stops reading further input and terminates normally without an error

### Requirement: Unrecognised commands are rejected
A non-blank line that does not match an available command SHALL be rejected with an error message and SHALL NOT stop the processing of later lines.

#### Scenario: Unknown command followed by HELP
- **WHEN** the lines `FOO` and `HELP` are processed
- **THEN** an error message is shown for `FOO` and the help text is shown for `HELP`

### Requirement: Controlled handling of unprocessable input
Any command line that cannot be processed correctly SHALL be reported with an error message, SHALL NOT terminate the application, and SHALL NOT prevent the following lines from being processed. A command file that does not exist or cannot be read SHALL be reported with an error message instead of an uncontrolled failure.

#### Scenario: Unexpected arguments for a general command
- **WHEN** the line `HELP;extra` is processed, followed by `HELP`
- **THEN** an error message is shown for the first line and the help text is shown for the second

#### Scenario: Internal failure while processing a line
- **WHEN** processing a line raises an unexpected error
- **THEN** an error message is shown and the next line is processed

#### Scenario: Nonexistent command file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message naming the file is shown and the application terminates without a stack trace

### Requirement: Command fields separated by semicolons
Command lines SHALL be split into fields on the `;` character using basic string operations, the first field being the command keyword and the remaining fields its arguments.

#### Scenario: Keyword taken from first field
- **WHEN** the line `EXIT` is processed
- **THEN** it is recognised as the EXIT command with no arguments

#### Scenario: Surrounding whitespace ignored
- **WHEN** the line `  HELP  ` is processed
- **THEN** it is recognised as the HELP command

### Requirement: HELP lists invoice commands
The HELP output SHALL include the syntaxes `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: HELP shows INVOICE commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`

### Requirement: HELP EXTENDED command
The command `HELP EXTENDED` SHALL display, for every command recognized by the application, its syntax together with a brief explanation of its purpose, and SHALL NOT stop processing. `HELP` SHALL remain available, keep displaying the command syntax, and include `HELP EXTENDED` in its list.

#### Scenario: Extended help shows syntax and purpose
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax shown by `HELP` appears, each followed by a non-empty description of its purpose

#### Scenario: HELP lists HELP EXTENDED
- **WHEN** `HELP` is processed
- **THEN** the output contains `HELP EXTENDED`

#### Scenario: Extra arguments rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error message is shown and processing continues

### Requirement: Injected help-generation behavior
The component that generates help text SHALL receive, through dependency injection, the behavior used to build the output, so that new ways of generating help can be added without changing the logic that reads and executes commands.

#### Scenario: Alternative formatter injected
- **WHEN** the help generator is created with a different formatting behavior
- **THEN** HELP output follows that behavior while command reading and execution are unchanged

### Requirement: HELP lists pass commands
The HELP and HELP EXTENDED outputs SHALL include `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>` and `SPECIALPASS DELETE;<specialPassIdentifier>`.

#### Scenario: HELP shows pass commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains the four pass command syntaxes

### Requirement: HELP lists typed attraction creation
The HELP and HELP EXTENDED outputs SHALL include `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` in addition to the Delivery 2 syntax.

#### Scenario: HELP shows typed creation
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`

### Requirement: HELP lists SpeedPass entry
The HELP and HELP EXTENDED outputs SHALL include `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: HELP shows the SPEED variant
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`

### Requirement: HELP shows the Delivery 3 minimum set
The HELP output SHALL contain the commands of the three deliveries, including at least `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`; HELP EXTENDED SHALL describe each of them.

#### Scenario: Delivery 3 syntax present
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every syntax of the Delivery 3 minimum set appears in both, and in HELP EXTENDED each is followed by a description
