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
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `HELP` and `EXIT`.

#### Scenario: HELP lists the minimum command set
- **WHEN** the line `HELP` is processed
- **THEN** the output contains each of the syntaxes `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`

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

### Requirement: HELP lists group ticket commands
The HELP output SHALL include the syntaxes `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`.

#### Scenario: HELP shows GROUPTICKET commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`

### Requirement: HELP lists attraction commands
The HELP output SHALL include the syntaxes `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`

### Requirement: HELP lists attraction entry
The HELP output SHALL include the syntax `ATTRACTION ENTER;<attraction>;<ticket>`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`

### Requirement: HELP lists attraction exit and status
The HELP output SHALL include the syntaxes `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attraction>`.

#### Scenario: HELP shows ATTRACTION EXIT and STATUS
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attraction>`
